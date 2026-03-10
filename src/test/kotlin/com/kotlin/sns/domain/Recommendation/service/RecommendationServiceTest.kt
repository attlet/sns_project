package com.kotlin.sns.domain.Recommendation.service

import com.kotlin.sns.domain.Content.entity.ContentType
import com.kotlin.sns.domain.Recommendation.dto.response.RecommendationType
import com.kotlin.sns.domain.Recommendation.dto.response.ResponseRecommendationDto
import com.kotlin.sns.domain.Recommendation.repository.RecommendationQueryRepository
import com.kotlin.sns.domain.Recommendation.service.Impl.RecommendationServiceImpl
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * RecommendationService 단위 테스트
 *
 * MockK를 사용하여 RecommendationServiceImpl의 추천 로직을 테스트한다.
 */
@ExtendWith(MockKExtension::class)
class RecommendationServiceTest {

    private val recommendationQueryRepository: RecommendationQueryRepository = mockk()
    private lateinit var recommendationService: RecommendationService

    @BeforeEach
    fun setUp() {
        recommendationService = RecommendationServiceImpl(recommendationQueryRepository)
    }

    @Nested
    @DisplayName("getPopularRecommendations 테스트")
    inner class GetPopularRecommendationsTest {

        @Test
        @DisplayName("내가 평가하지 않은 콘텐츠만 반환 (평가한 콘텐츠 제외)")
        fun getPopularRecommendations_ExcludesRatedContent() {
            // Given
            // Content A(contentId=1L)는 내가 평가함 → repository가 반환하지 않음 (쿼리 레벨에서 필터)
            // Content B(contentId=2L)는 미평가 → repository가 반환함
            val memberId = 1L
            val contentBDto = ResponseRecommendationDto(
                contentId = 2L,
                title = "Content B",
                type = ContentType.GAME,
                thumbnailUrl = null,
                avgRating = 4.0,
                reviewCount = 20,
                recommendationType = RecommendationType.POPULAR
            )
            every {
                recommendationQueryRepository.findPopularContents(memberId, ContentType.GAME, 10)
            } returns listOf(contentBDto)

            // When
            val result = recommendationService.getPopularRecommendations(memberId, ContentType.GAME, 10)

            // Then
            assertAll(
                { assertEquals(1, result.size) },
                { assertEquals(2L, result[0].contentId) },             // Content B만 포함
                { assertFalse(result.any { it.contentId == 1L }) }      // Content A 없음
            )
            verify(exactly = 1) {
                recommendationQueryRepository.findPopularContents(memberId, ContentType.GAME, 10)
            }
        }

        @Test
        @DisplayName("타입 필터 적용 - GAME만 반환, ANIME 제외")
        fun getPopularRecommendations_TypeFilter_OnlyGameReturned() {
            // Given
            // GAME Content B(id=2L), ANIME Content C(id=3L) 둘 다 미평가
            // type=GAME 조건으로 repository 호출 시 B만 반환
            val memberId = 1L
            val gameDtoB = ResponseRecommendationDto(
                contentId = 2L,
                title = "Content B",
                type = ContentType.GAME,
                thumbnailUrl = null,
                avgRating = 4.0,
                reviewCount = 10,
                recommendationType = RecommendationType.POPULAR
            )
            every {
                recommendationQueryRepository.findPopularContents(memberId, ContentType.GAME, 10)
            } returns listOf(gameDtoB)

            // When
            val result = recommendationService.getPopularRecommendations(memberId, ContentType.GAME, 10)

            // Then
            assertAll(
                { assertEquals(1, result.size) },
                { assertEquals(ContentType.GAME, result[0].type) },
                { assertFalse(result.any { it.type == ContentType.ANIME }) }
            )
            verify(exactly = 1) {
                recommendationQueryRepository.findPopularContents(memberId, ContentType.GAME, 10)
            }
        }

        @Test
        @DisplayName("limit=5 적용 - 최대 5개 반환")
        fun getPopularRecommendations_LimitApplied() {
            // Given
            val memberId = 1L
            val limit = 5
            val fiveDtos = (1..5).map { i ->
                ResponseRecommendationDto(
                    contentId = i.toLong(),
                    title = "Content $i",
                    type = ContentType.GAME,
                    thumbnailUrl = null,
                    avgRating = 4.0,
                    reviewCount = 10,
                    recommendationType = RecommendationType.POPULAR
                )
            }
            every {
                recommendationQueryRepository.findPopularContents(memberId, null, limit)
            } returns fiveDtos

            // When
            val result = recommendationService.getPopularRecommendations(memberId, null, limit)

            // Then
            assertEquals(5, result.size)
            verify(exactly = 1) {
                recommendationQueryRepository.findPopularContents(memberId, null, limit)
            }
        }
    }

    @Nested
    @DisplayName("getCfRecommendations 테스트")
    inner class GetCfRecommendationsTest {

        @Test
        @DisplayName("유사 사용자의 고평점 콘텐츠 추천 - 내 미평가 콘텐츠 반환")
        fun getCfRecommendations_ReturnsSimilarUserHighRatedContent() {
            // Given
            // 나(A): Content X 평가함
            // 유사 사용자(B): Content X(4점), Content Y(5점) 평가
            // 쿼리 레벨에서 내 평가(X) 제외 → Y만 반환
            val memberId = 1L
            val contentYDto = ResponseRecommendationDto(
                contentId = 2L,
                title = "Content Y",
                type = ContentType.GAME,
                thumbnailUrl = null,
                avgRating = 5.0,
                reviewCount = 5,
                recommendationType = RecommendationType.COLLABORATIVE_FILTERING
            )
            every {
                recommendationQueryRepository.findCfContents(memberId, null, 10)
            } returns listOf(contentYDto)

            // When
            val result = recommendationService.getCfRecommendations(memberId, null, 10)

            // Then
            assertAll(
                { assertEquals(1, result.size) },
                { assertEquals(2L, result[0].contentId) },
                { assertEquals(RecommendationType.COLLABORATIVE_FILTERING, result[0].recommendationType) }
            )
            verify(exactly = 1) {
                recommendationQueryRepository.findCfContents(memberId, null, 10)
            }
        }

        @Test
        @DisplayName("이미 평가한 콘텐츠 제외 - 미평가 Z만 반환")
        fun getCfRecommendations_ExcludesAlreadyRatedContent() {
            // Given
            // 나(A): Content X(4점), Y(5점) 평가
            // 유사 사용자(B): X(4점), Y(5점), Z(5점) 평가
            // 쿼리 레벨에서 X, Y(내 평가 콘텐츠) 제외 → Z만 반환
            val memberId = 1L
            val contentZDto = ResponseRecommendationDto(
                contentId = 3L,
                title = "Content Z",
                type = ContentType.GAME,
                thumbnailUrl = null,
                avgRating = 5.0,
                reviewCount = 5,
                recommendationType = RecommendationType.COLLABORATIVE_FILTERING
            )
            every {
                recommendationQueryRepository.findCfContents(memberId, null, 10)
            } returns listOf(contentZDto)

            // When
            val result = recommendationService.getCfRecommendations(memberId, null, 10)

            // Then
            assertAll(
                { assertEquals(1, result.size) },
                { assertEquals(3L, result[0].contentId) },             // Z만 포함
                { assertFalse(result.any { it.contentId == 1L }) },     // X 없음
                { assertFalse(result.any { it.contentId == 2L }) }      // Y 없음
            )
            verify(exactly = 1) {
                recommendationQueryRepository.findCfContents(memberId, null, 10)
            }
        }
    }

    @Nested
    @DisplayName("getRecommendations 테스트 (CF + 인기도 폴백)")
    inner class GetRecommendationsTest {

        @Test
        @DisplayName("유사 사용자 없을 때 인기도 기반 폴백 반환")
        fun getRecommendations_NoSimilarUsers_FallbackToPopular() {
            // Given
            // 평가 이력은 있지만 공통 평가 사용자 없음 → CF 결과 없음
            val memberId = 1L
            val popularDto = ResponseRecommendationDto(
                contentId = 10L,
                title = "Popular Content",
                type = ContentType.GAME,
                thumbnailUrl = null,
                avgRating = 4.5,
                reviewCount = 100,
                recommendationType = RecommendationType.POPULAR
            )
            every { recommendationQueryRepository.findCfContents(memberId, null, 10) } returns emptyList()
            every { recommendationQueryRepository.findPopularContents(memberId, null, 10) } returns listOf(popularDto)

            // When
            val result = recommendationService.getRecommendations(memberId, null, 10)

            // Then
            assertAll(
                { assertTrue(result.isNotEmpty()) },
                { assertEquals(RecommendationType.POPULAR, result[0].recommendationType) }
            )
            verify(exactly = 1) { recommendationQueryRepository.findCfContents(memberId, null, 10) }
            verify(exactly = 1) { recommendationQueryRepository.findPopularContents(memberId, null, 10) }
        }

        @Test
        @DisplayName("신규 사용자 (리뷰 없음) - 인기도 기반 폴백으로 전체 인기 콘텐츠 반환")
        fun getRecommendations_NewUser_FallbackToPopular() {
            // Given
            // 신규 사용자: 평가 이력 없음 → CF 유사 사용자 탐색 불가 → 빈 결과
            val memberId = 99L
            val popularDtos = (1..3).map { i ->
                ResponseRecommendationDto(
                    contentId = i.toLong(),
                    title = "Popular $i",
                    type = ContentType.GAME,
                    thumbnailUrl = null,
                    avgRating = 4.0,
                    reviewCount = 50,
                    recommendationType = RecommendationType.POPULAR
                )
            }
            every { recommendationQueryRepository.findCfContents(memberId, null, 10) } returns emptyList()
            every { recommendationQueryRepository.findPopularContents(memberId, null, 10) } returns popularDtos

            // When
            val result = recommendationService.getRecommendations(memberId, null, 10)

            // Then
            assertAll(
                { assertEquals(3, result.size) },
                { assertTrue(result.all { it.recommendationType == RecommendationType.POPULAR }) }
            )
            verify(exactly = 1) { recommendationQueryRepository.findCfContents(memberId, null, 10) }
            verify(exactly = 1) { recommendationQueryRepository.findPopularContents(memberId, null, 10) }
        }
    }
}
