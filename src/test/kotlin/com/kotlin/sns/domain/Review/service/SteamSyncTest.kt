package com.kotlin.sns.domain.Review.service

import com.kotlin.sns.common.exception.CustomException
import com.kotlin.sns.common.exception.ErrorCode
import com.kotlin.sns.domain.Content.dto.response.ResponseContentDto
import com.kotlin.sns.domain.Content.entity.Content
import com.kotlin.sns.domain.Content.entity.ContentType
import com.kotlin.sns.domain.Content.repository.ContentRepository
import com.kotlin.sns.domain.Content.service.ContentService
import com.kotlin.sns.domain.PlatformActivityRecord.entity.PlatformActivityRecord
import com.kotlin.sns.domain.PlatformActivityRecord.repository.PlatformActivityRecordRepository
import com.kotlin.sns.domain.PlatformActivityRecord.service.ExternalLibrarySyncService
import com.kotlin.sns.domain.PlatformActivityRecord.service.Impl.ExternalLibrarySyncServiceImpl
import com.kotlin.sns.domain.Member.entity.Member
import com.kotlin.sns.domain.Member.repository.MemberRepository
import com.kotlin.sns.domain.Review.entity.Review
import com.kotlin.sns.domain.Review.entity.ReviewSource
import com.kotlin.sns.domain.Review.entity.ReviewStatus
import com.kotlin.sns.domain.Review.helper.SteamRatingConverter
import com.kotlin.sns.domain.Review.helper.SteamRatingProperties
import com.kotlin.sns.domain.Review.repository.ReviewRepository
import com.kotlin.sns.infrastructure.external.igdb.IgdbClient
import com.kotlin.sns.infrastructure.external.igdb.dto.IgdbGameDto
import com.kotlin.sns.infrastructure.external.steam.SteamClient
import com.kotlin.sns.infrastructure.external.steam.dto.SteamGameDto
import io.mockk.*
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Optional

/**
 * syncSteamLibrary 서비스 단위 테스트
 *
 * ExternalLibrarySyncServiceImpl의 Steam 라이브러리 동기화 비즈니스 로직을 MockK로 검증한다.
 */
@ExtendWith(MockKExtension::class)
@DisplayName("syncSteamLibrary 테스트")
class SteamSyncTest {

    private val reviewRepository: ReviewRepository = mockk()
    private val memberRepository: MemberRepository = mockk()
    private val contentRepository: ContentRepository = mockk()
    private val steamClient: SteamClient = mockk()
    private val igdbClient: IgdbClient = mockk()
    private val contentService: ContentService = mockk()
    private val platformActivityRecordRepository: PlatformActivityRecordRepository = mockk()
    private val steamRatingConverter = SteamRatingConverter(
        SteamRatingProperties(SteamRatingProperties.Thresholds(599, 2999, 11999))
    )

    private lateinit var syncService: ExternalLibrarySyncService

    companion object {
        private const val MEMBER_ID = 1L
        private const val STEAM_ID = "76561198000000000"
        private const val CONTENT_ID = 10L
    }

    @BeforeEach
    fun setUp() {
        syncService = ExternalLibrarySyncServiceImpl(
            memberRepository, contentRepository, contentService,
            steamClient, igdbClient, steamRatingConverter,
            platformActivityRecordRepository, reviewRepository
        )
    }

    /** Member mock - steamId 세팅 포함 */
    private fun memberWithSteamId(steamId: String?): Member {
        val member: Member = mockk(relaxed = true)
        every { member.id } returns MEMBER_ID
        every { member.steamId } returns steamId
        return member
    }

    /** IGDB 매핑 성공 → Content 반환 stub */
    private fun stubIgdbContentResolution(content: Content) {
        val igdbGame = IgdbGameDto(id = 1942L, name = "The Witcher 3: Wild Hunt")
        val contentDto = ResponseContentDto(
            id = CONTENT_ID, type = ContentType.GAME, title = "The Witcher 3: Wild Hunt",
            description = null, releaseYear = null, thumbnailUrl = null,
            steamAppId = null, igdbId = 1942L, malId = null, anilistId = null
        )
        every { igdbClient.getGameBySteamAppId(292030L) } returns igdbGame
        every { contentService.importGameFromIgdb(1942L) } returns contentDto
        every { contentRepository.findById(CONTENT_ID) } returns Optional.of(content)
    }

    // ─────────────────────────────────────────────────────────
    // Test 4: 신규 Review 생성 (playtime > 0)
    // ─────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Test 4: 신규 Review 생성")
    inner class CreateNewReviewTest {

        @Test
        @DisplayName("기존 STEAM Review 없을 때 PlatformActivityRecord + Review 신규 생성")
        fun `given no existing record when syncSteamLibrary then create record and review`() {
            // Given
            val member = memberWithSteamId(STEAM_ID)
            val content: Content = mockk(relaxed = true)
            every { content.id } returns CONTENT_ID

            every { memberRepository.findById(MEMBER_ID) } returns Optional.of(member)
            every { steamClient.getOwnedGames(STEAM_ID) } returns listOf(
                SteamGameDto(appId = 292030, name = "The Witcher 3: Wild Hunt", playtimeMinutes = 4500)
            )
            stubIgdbContentResolution(content)
            every {
                platformActivityRecordRepository.findActiveByMemberAndContentAndSource(MEMBER_ID, CONTENT_ID, ReviewSource.STEAM)
            } returns null
            every { platformActivityRecordRepository.save(any()) } returns mockk(relaxed = true)
            every {
                reviewRepository.findActiveByMemberAndContentAndSource(MEMBER_ID, CONTENT_ID, ReviewSource.STEAM)
            } returns null
            every { reviewRepository.save(any()) } returns mockk(relaxed = true)

            // When
            val result = syncService.syncSteamLibrary(MEMBER_ID)

            // Then: PlatformActivityRecord 저장 검증
            val recordSlot = slot<PlatformActivityRecord>()
            verify(exactly = 1) { platformActivityRecordRepository.save(capture(recordSlot)) }
            assertAll(
                { assertEquals(ReviewSource.STEAM, recordSlot.captured.source) },
                { assertEquals(4500, recordSlot.captured.playtimeMinutes) },
                { assertNotNull(recordSlot.captured.syncedAt) }
            )

            // Then: Review 저장 검증 (playtime > 0이므로 생성)
            val reviewSlot = slot<Review>()
            verify(exactly = 1) { reviewRepository.save(capture(reviewSlot)) }
            assertAll(
                { assertEquals(ReviewSource.STEAM, reviewSlot.captured.source) },
                { assertEquals(4, reviewSlot.captured.rating) },          // 4500분 → tier4 → rating=4
                { assertEquals(ReviewStatus.PLAYED, reviewSlot.captured.status) },
                { assertEquals(1, result.synced) },
                { assertEquals(1, result.created) },
                { assertEquals(0, result.updated) }
            )
        }
    }

    // ─────────────────────────────────────────────────────────
    // Test 5: 기존 Review 업데이트
    // ─────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Test 5: 기존 Review 업데이트")
    inner class UpdateExistingReviewTest {

        @Test
        @DisplayName("기존 STEAM Record + Review 존재 시 playtime/rating 갱신")
        fun `given existing steam record when syncSteamLibrary then update without new save`() {
            // Given
            val member = memberWithSteamId(STEAM_ID)
            val content: Content = mockk(relaxed = true)
            every { content.id } returns CONTENT_ID

            val existingRecord: PlatformActivityRecord = mockk(relaxed = true)
            every { existingRecord.playtimeMinutes = any() } just Runs
            every { existingRecord.externalRating = any() } just Runs
            every { existingRecord.syncedAt = any() } just Runs

            val existingReview = Review(
                member = mockk(relaxed = true), content = content,
                rating = 4, status = ReviewStatus.PLAYED, source = ReviewSource.STEAM
            )

            every { memberRepository.findById(MEMBER_ID) } returns Optional.of(member)
            every { steamClient.getOwnedGames(STEAM_ID) } returns listOf(
                SteamGameDto(appId = 292030, name = "The Witcher 3: Wild Hunt", playtimeMinutes = 15000)
            )
            stubIgdbContentResolution(content)
            every {
                platformActivityRecordRepository.findActiveByMemberAndContentAndSource(MEMBER_ID, CONTENT_ID, ReviewSource.STEAM)
            } returns existingRecord
            every {
                reviewRepository.findActiveByMemberAndContentAndSource(MEMBER_ID, CONTENT_ID, ReviewSource.STEAM)
            } returns existingReview

            // When
            val result = syncService.syncSteamLibrary(MEMBER_ID)

            // Then: save 호출 없음, 필드 직접 갱신 확인
            verify(exactly = 0) { platformActivityRecordRepository.save(any()) }
            verify(exactly = 0) { reviewRepository.save(any()) }
            assertAll(
                { assertEquals(5, existingReview.rating) },             // 15000분 → rating=5
                { assertEquals(1, result.synced) },
                { assertEquals(0, result.created) },
                { assertEquals(1, result.updated) }
            )
        }
    }

    // ─────────────────────────────────────────────────────────
    // Test 6: playtime=0 → Review 생성 안 함 (PlatformActivityRecord만 저장)
    // ─────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Test 6: playtime=0 → Review 생성 안 함")
    inner class NoReviewForUnplayedGameTest {

        @Test
        @DisplayName("playtime=0인 게임은 PlatformActivityRecord만 저장하고 Review는 생성하지 않음")
        fun `given playtime 0 when syncSteamLibrary then save record only without review`() {
            // Given
            val member = memberWithSteamId(STEAM_ID)
            val content: Content = mockk(relaxed = true)
            every { content.id } returns CONTENT_ID

            every { memberRepository.findById(MEMBER_ID) } returns Optional.of(member)
            every { steamClient.getOwnedGames(STEAM_ID) } returns listOf(
                SteamGameDto(appId = 292030, name = "The Witcher 3: Wild Hunt", playtimeMinutes = 0)
            )
            stubIgdbContentResolution(content)
            every {
                platformActivityRecordRepository.findActiveByMemberAndContentAndSource(MEMBER_ID, CONTENT_ID, ReviewSource.STEAM)
            } returns null
            every { platformActivityRecordRepository.save(any()) } returns mockk(relaxed = true)

            // When
            val result = syncService.syncSteamLibrary(MEMBER_ID)

            // Then: PlatformActivityRecord 저장, Review 저장 없음
            verify(exactly = 1) { platformActivityRecordRepository.save(any()) }
            verify(exactly = 0) { reviewRepository.save(any()) }
            assertAll(
                { assertEquals(1, result.synced) },
                { assertEquals(0, result.created) },
                { assertEquals(0, result.updated) }
            )
        }
    }

    // ─────────────────────────────────────────────────────────
    // Test 7: Steam ID 미연결
    // ─────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Test 7: Steam ID 미연결")
    inner class SteamIdNotLinkedTest {

        @Test
        @DisplayName("steamId=null인 Member 동기화 시 STEAM_ID_NOT_LINKED 예외")
        fun `given member without steamId when syncSteamLibrary then throw STEAM_ID_NOT_LINKED`() {
            // Given
            val member = memberWithSteamId(null)
            every { memberRepository.findById(MEMBER_ID) } returns Optional.of(member)

            // When & Then
            val exception = assertThrows<CustomException> {
                syncService.syncSteamLibrary(MEMBER_ID)
            }
            assertEquals(ErrorCode.STEAM_ID_NOT_LINKED, exception.errorCode)
        }
    }

    // ─────────────────────────────────────────────────────────
    // Test 8: IGDB 매핑 없는 게임 → Content 직접 생성
    // ─────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Test 8: IGDB 매핑 없는 게임 → Content 직접 생성")
    inner class IgdbNotFoundTest {

        @Test
        @DisplayName("IGDB 매핑 없을 때 게임명 + type=GAME으로 Content 직접 생성 후 Record/Review 연결")
        fun `given no igdb mapping when syncSteamLibrary then create content by game name`() {
            // Given
            val member = memberWithSteamId(STEAM_ID)
            val content: Content = mockk(relaxed = true)
            every { content.id } returns 20L
            val contentDto = ResponseContentDto(
                id = 20L, type = ContentType.GAME, title = "Indie Game",
                description = null, releaseYear = null, thumbnailUrl = null,
                steamAppId = null, igdbId = null, malId = null, anilistId = null
            )

            every { memberRepository.findById(MEMBER_ID) } returns Optional.of(member)
            every { steamClient.getOwnedGames(STEAM_ID) } returns listOf(
                SteamGameDto(appId = 99999, name = "Indie Game", playtimeMinutes = 600)
            )
            every { igdbClient.getGameBySteamAppId(99999L) } returns null
            every { contentService.create(any()) } returns contentDto
            every { contentRepository.findById(20L) } returns Optional.of(content)
            every {
                platformActivityRecordRepository.findActiveByMemberAndContentAndSource(MEMBER_ID, 20L, ReviewSource.STEAM)
            } returns null
            every { platformActivityRecordRepository.save(any()) } returns mockk(relaxed = true)
            every {
                reviewRepository.findActiveByMemberAndContentAndSource(MEMBER_ID, 20L, ReviewSource.STEAM)
            } returns null
            every { reviewRepository.save(any()) } returns mockk(relaxed = true)

            // When
            syncService.syncSteamLibrary(MEMBER_ID)

            // Then: 게임명 + GAME 타입으로 Content 생성 검증
            verify { contentService.create(match { it.type == ContentType.GAME && it.title == "Indie Game" }) }
            verify(exactly = 1) { platformActivityRecordRepository.save(any()) }
            verify(exactly = 1) { reviewRepository.save(any()) }
        }
    }
}
