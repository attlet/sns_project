package com.kotlin.sns.domain.Review.service

import com.kotlin.sns.common.exception.CustomException
import com.kotlin.sns.common.exception.ErrorCode
import com.kotlin.sns.domain.Content.entity.Content
import com.kotlin.sns.domain.Member.entity.Member
import com.kotlin.sns.domain.Review.dto.response.ResponseReviewDto
import com.kotlin.sns.domain.Review.entity.Review
import com.kotlin.sns.domain.Review.entity.ReviewSource
import com.kotlin.sns.domain.Review.entity.ReviewStatus
import com.kotlin.sns.domain.Review.mapper.ReviewMapper
import com.kotlin.sns.domain.Content.repository.ContentRepository
import com.kotlin.sns.domain.Member.repository.MemberRepository
import com.kotlin.sns.domain.Review.repository.ReviewRepository
import com.kotlin.sns.domain.Review.service.Impl.ReviewServiceImpl
import io.mockk.*
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant

/**
 * ReviewService 단위 테스트
 *
 * MockK를 사용하여 ReviewServiceImpl의 비즈니스 로직을 테스트한다.
 */
@ExtendWith(MockKExtension::class)
class ReviewServiceTest {

    private val reviewRepository: ReviewRepository = mockk()
    private val memberRepository: MemberRepository = mockk()
    private val contentRepository: ContentRepository = mockk()
    private lateinit var reviewService: ReviewService

    @BeforeEach
    fun setUp() {
        reviewService = ReviewServiceImpl(reviewRepository, memberRepository, contentRepository)
        mockkObject(ReviewMapper)
    }

    @Nested
    @DisplayName("getReviewById 테스트")
    inner class GetReviewByIdTest {

        @Test
        @DisplayName("존재하는 리뷰 조회 - 성공")
        fun getReviewById_Success() {
            // Given
            val reviewId = 1L
            val review: Review = mockk(relaxed = true)
            val responseDto = ResponseReviewDto(
                id = reviewId,
                memberId = 10L,
                contentId = 20L,
                rating = 4,
                status = ReviewStatus.PLAYED,
                comment = "재밌었다",
                source = ReviewSource.MANUAL,
                createdDt = Instant.now()
            )

            every { reviewRepository.findByIdAndIsDeletedFalse(reviewId) } returns review
            every { ReviewMapper.toDto(review) } returns responseDto

            // When
            val result = reviewService.getReviewById(reviewId)

            // Then
            assertAll(
                { assertEquals(reviewId, result.id) },
                { assertEquals(10L, result.memberId) },
                { assertEquals(20L, result.contentId) },
                { assertEquals(4, result.rating) },
                { assertEquals(ReviewStatus.PLAYED, result.status) },
                { assertEquals("재밌었다", result.comment) },
                { assertEquals(ReviewSource.MANUAL, result.source) }
            )
            verify(exactly = 1) { reviewRepository.findByIdAndIsDeletedFalse(reviewId) }
        }

        @Test
        @DisplayName("존재하지 않는 리뷰 조회 - REVIEW_NOT_FOUND 예외")
        fun getReviewById_NotFound() {
            // Given
            val reviewId = 999L
            every { reviewRepository.findByIdAndIsDeletedFalse(reviewId) } returns null

            // When & Then
            val exception = assertThrows(CustomException::class.java) {
                reviewService.getReviewById(reviewId)
            }
            assertEquals(ErrorCode.REVIEW_NOT_FOUND, exception.errorCode)
            verify(exactly = 1) { reviewRepository.findByIdAndIsDeletedFalse(reviewId) }
        }

        @Test
        @DisplayName("삭제된 리뷰 조회 - REVIEW_NOT_FOUND 예외")
        fun getReviewById_DeletedReview() {
            // Given
            val reviewId = 1L
            every { reviewRepository.findByIdAndIsDeletedFalse(reviewId) } returns null

            // When & Then
            val exception = assertThrows(CustomException::class.java) {
                reviewService.getReviewById(reviewId)
            }
            assertEquals(ErrorCode.REVIEW_NOT_FOUND, exception.errorCode)
            verify(exactly = 1) { reviewRepository.findByIdAndIsDeletedFalse(reviewId) }
        }
    }
}
