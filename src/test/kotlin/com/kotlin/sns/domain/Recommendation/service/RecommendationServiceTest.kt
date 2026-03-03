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
    }
}
