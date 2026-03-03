package com.kotlin.sns.domain.ExternalLibrary.service.Impl

import com.kotlin.sns.common.exception.CustomException
import com.kotlin.sns.common.exception.ErrorCode
import com.kotlin.sns.domain.Content.dto.request.RequestCreateContentDto
import com.kotlin.sns.domain.Content.entity.Content
import com.kotlin.sns.domain.Content.entity.ContentType
import com.kotlin.sns.domain.Content.repository.ContentRepository
import com.kotlin.sns.domain.Content.service.ContentService
import com.kotlin.sns.domain.ExternalLibrary.entity.ExternalLibraryRecord
import com.kotlin.sns.domain.ExternalLibrary.repository.ExternalLibraryRecordRepository
import com.kotlin.sns.domain.ExternalLibrary.service.ExternalLibrarySyncService
import com.kotlin.sns.domain.Member.repository.MemberRepository
import com.kotlin.sns.domain.Review.dto.response.SteamSyncResultDto
import com.kotlin.sns.domain.Review.entity.Review
import com.kotlin.sns.domain.Review.entity.ReviewSource
import com.kotlin.sns.domain.Review.entity.ReviewStatus
import com.kotlin.sns.domain.Review.helper.SteamRatingConverter
import com.kotlin.sns.domain.Review.repository.ReviewRepository
import com.kotlin.sns.infrastructure.external.igdb.IgdbClient
import com.kotlin.sns.infrastructure.external.steam.SteamClient
import com.kotlin.sns.infrastructure.external.steam.dto.SteamGameDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * 외부 라이브러리 동기화 서비스 구현체
 *
 * Steam 라이브러리 동기화 비즈니스 로직을 제공한다.
 * 외부 플랫폼 원본 데이터는 ExternalLibraryRecord에, 순수 평가 데이터는 Review에 분리 저장한다.
 *
 * @property memberRepository
 * @property contentRepository
 * @property contentService
 * @property steamClient
 * @property igdbClient
 * @property steamRatingConverter
 * @property externalLibraryRecordRepository
 * @property reviewRepository
 */
@Service
class ExternalLibrarySyncServiceImpl(
    private val memberRepository: MemberRepository,
    private val contentRepository: ContentRepository,
    private val contentService: ContentService,
    private val steamClient: SteamClient,
    private val igdbClient: IgdbClient,
    private val steamRatingConverter: SteamRatingConverter,
    private val externalLibraryRecordRepository: ExternalLibraryRecordRepository,
    private val reviewRepository: ReviewRepository
) : ExternalLibrarySyncService {

    /**
     * Steam 라이브러리 동기화
     *
     * 각 Steam 게임에 대해:
     * 1. Content 획득/생성
     * 2. ExternalLibraryRecord upsert (playtime, externalRating, syncedAt)
     * 3. playtime > 0인 경우에만 Review upsert (rating, status, source=STEAM)
     *    playtime == 0인 미플레이 게임은 소유 기록만 남기고 Review는 생성하지 않음
     *
     * @param memberId 동기화 대상 회원 ID
     * @return 동기화 결과 (전체/생성/업데이트 수)
     */
    @Transactional
    override fun syncSteamLibrary(memberId: Long): SteamSyncResultDto {

        // 1. member 조회
        val member = memberRepository.findById(memberId)
            .orElseThrow { CustomException(ErrorCode.MEMBER_NOT_FOUND) }

        // 2. member의 steam id 조회, null 체크
        val steamId = member.steamId
            ?: throw CustomException(ErrorCode.STEAM_ID_NOT_LINKED)

        // 3. 해당 사용자의 steam id 기반으로 소유한 게임 목록 조회
        val games = steamClient.getOwnedGames(steamId)

        var created = 0
        var updated = 0

        for (game in games) {
            // 4. 사용자 소유 게임들을 igdb 연동을 통해 content 엔티티로 매핑, 저장한다.
            val content = resolveContent(game)

            // 5. ExternalLibraryRecord upsert — 모든 게임의 동기화 메타데이터 저장
            val existingRecord = externalLibraryRecordRepository.findActiveByMemberAndContentAndSource(
                member.id, content.id, ReviewSource.STEAM
            )
            if (existingRecord == null) {
                externalLibraryRecordRepository.save(
                    ExternalLibraryRecord(
                        member = member,
                        content = content,
                        source = ReviewSource.STEAM,
                        playtimeMinutes = game.playtimeMinutes,
                        externalRating = null,  // Steam API는 별도 평점 미제공
                        syncedAt = Instant.now()
                    )
                )
            } else {
                existingRecord.playtimeMinutes = game.playtimeMinutes
                existingRecord.externalRating = null  // Steam API는 별도 평점 미제공
                existingRecord.syncedAt = Instant.now()
            }

            // 6. playtime == 0인 미플레이 게임은 Review를 생성하지 않음 (소유만 기록)
            if (game.playtimeMinutes == 0) continue

            // 7. 사용자가 해당 content를 플레이한 시간을 기준으로 rating을 계산
            val rating = steamRatingConverter.convert(game.playtimeMinutes)

            // 8. Review upsert — 실제 플레이한 게임만 평가 데이터 저장
            val existingReview = reviewRepository.findActiveByMemberAndContentAndSource(
                member.id, content.id, ReviewSource.STEAM
            )
            if (existingReview == null) {
                reviewRepository.save(
                    Review(
                        member = member,
                        content = content,
                        rating = rating,
                        status = ReviewStatus.PLAYED,
                        source = ReviewSource.STEAM
                    )
                )
                created++
            } else {
                existingReview.rating = rating
                updated++
            }
        }

        return SteamSyncResultDto(synced = games.size, created = created, updated = updated)
    }

    /**
     * Steam 게임에 매핑되는 Content를 조회하거나 생성한다.
     *
     * IGDB 매핑 성공 시 importGameFromIgdb, 실패 시 게임명으로 직접 생성.
     */
    private fun resolveContent(game: SteamGameDto): Content {
        val igdbGame = igdbClient.getGameBySteamAppId(game.appId.toLong())

        val contentDto = if (igdbGame != null) {
            contentService.importGameFromIgdb(igdbGame.id)
        } else {
            contentService.create(RequestCreateContentDto(type = ContentType.GAME, title = game.name))
        }

        return contentRepository.findById(contentDto.id)
            .orElseThrow { CustomException(ErrorCode.CONTENT_NOT_FOUND) }
    }
}
