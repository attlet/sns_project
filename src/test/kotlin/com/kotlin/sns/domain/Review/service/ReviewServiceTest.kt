package com.kotlin.sns.domain.Review.service

import com.kotlin.sns.common.exception.CustomException
import com.kotlin.sns.common.exception.ErrorCode
import com.kotlin.sns.domain.Content.entity.Content
import com.kotlin.sns.domain.Member.entity.Member
import com.kotlin.sns.domain.Review.dto.request.RequestCreateReviewDto
import com.kotlin.sns.domain.Review.dto.request.RequestUpdateReviewDto
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
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.Instant
import java.util.Optional

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

            every { reviewRepository.findById(reviewId) } returns Optional.of(review)
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
            verify(exactly = 1) { reviewRepository.findById(reviewId) }
        }

        @Test
        @DisplayName("존재하지 않는 리뷰 조회 - REVIEW_NOT_FOUND 예외")
        fun getReviewById_NotFound() {
            // Given
            val reviewId = 999L
            every { reviewRepository.findById(reviewId) } returns Optional.empty()

            // When & Then
            val exception = assertThrows(CustomException::class.java) {
                reviewService.getReviewById(reviewId)
            }
            assertEquals(ErrorCode.REVIEW_NOT_FOUND, exception.errorCode)
            verify(exactly = 1) { reviewRepository.findById(reviewId) }
        }

        @Test
        @DisplayName("삭제된 리뷰 조회 - REVIEW_NOT_FOUND 예외")
        fun getReviewById_DeletedReview() {
            // Given
            val reviewId = 1L
            every { reviewRepository.findById(reviewId) } returns Optional.empty()

            // When & Then
            val exception = assertThrows(CustomException::class.java) {
                reviewService.getReviewById(reviewId)
            }
            assertEquals(ErrorCode.REVIEW_NOT_FOUND, exception.errorCode)
            verify(exactly = 1) { reviewRepository.findById(reviewId) }
        }
    }

    @Nested
    @DisplayName("getReviewsByMember 테스트")
    inner class GetReviewsByMemberTest {

        @Test
        @DisplayName("Member의 전체 리뷰 목록 페이징 조회 - 성공")
        fun getReviewsByMember_Success() {
            // Given
            val memberId = 1L
            val pageable = PageRequest.of(0, 10)
            val review: Review = mockk(relaxed = true)
            val responseDto = ResponseReviewDto(
                id = 1L, memberId = memberId, contentId = 20L,
                rating = 4, status = ReviewStatus.PLAYED, comment = null,
                source = ReviewSource.MANUAL, createdDt = Instant.now()
            )
            val reviewPage: Page<Review> = PageImpl(listOf(review), pageable, 1)

            every { reviewRepository.findReviewsByMember(memberId, null, pageable) } returns reviewPage
            every { ReviewMapper.toDto(review) } returns responseDto

            // When
            val result = reviewService.getReviewsByMember(memberId, null, pageable)

            // Then
            assertAll(
                { assertEquals(1L, result.totalElements) },
                { assertEquals(1, result.content.size) },
                { assertEquals(memberId, result.content[0].memberId) }
            )
            verify(exactly = 1) { reviewRepository.findReviewsByMember(memberId, null, pageable) }
        }

        @Test
        @DisplayName("status=FAVORITE 필터로 리뷰 목록 조회 - 성공")
        fun getReviewsByMember_WithFavoriteFilter_Success() {
            // Given
            val memberId = 1L
            val status = ReviewStatus.FAVORITE
            val pageable = PageRequest.of(0, 10)
            val review: Review = mockk(relaxed = true)
            val responseDto = ResponseReviewDto(
                id = 1L, memberId = memberId, contentId = 20L,
                rating = 5, status = ReviewStatus.FAVORITE, comment = null,
                source = ReviewSource.MANUAL, createdDt = Instant.now()
            )
            val reviewPage: Page<Review> = PageImpl(listOf(review), pageable, 1)

            every { reviewRepository.findReviewsByMember(memberId, status, pageable) } returns reviewPage
            every { ReviewMapper.toDto(review) } returns responseDto

            // When
            val result = reviewService.getReviewsByMember(memberId, status, pageable)

            // Then
            assertAll(
                { assertEquals(1L, result.totalElements) },
                { assertEquals(ReviewStatus.FAVORITE, result.content[0].status) }
            )
            verify(exactly = 1) { reviewRepository.findReviewsByMember(memberId, status, pageable) }
        }
    }

    @Nested
    @DisplayName("getReviewsByContent 테스트")
    inner class GetReviewsByContentTest {

        @Test
        @DisplayName("Content의 리뷰 목록 페이징 조회 - 성공")
        fun getReviewsByContent_Success() {
            // Given
            val contentId = 10L
            val pageable = PageRequest.of(0, 10)
            val review: Review = mockk(relaxed = true)
            val responseDto = ResponseReviewDto(
                id = 1L, memberId = 1L, contentId = contentId,
                rating = 4, status = ReviewStatus.PLAYED, comment = null,
                source = ReviewSource.MANUAL, createdDt = Instant.now()
            )
            val reviewPage: Page<Review> = PageImpl(listOf(review), pageable, 1)

            every { reviewRepository.findReviewsByContent(contentId, pageable) } returns reviewPage
            every { ReviewMapper.toDto(review) } returns responseDto

            // When
            val result = reviewService.getReviewsByContent(contentId, pageable)

            // Then
            assertAll(
                { assertEquals(1L, result.totalElements) },
                { assertEquals(contentId, result.content[0].contentId) }
            )
            verify(exactly = 1) { reviewRepository.findReviewsByContent(contentId, pageable) }
        }
    }

    @Nested
    @DisplayName("createReview 테스트")
    inner class CreateReviewTest {

        @Test
        @DisplayName("리뷰 생성 성공")
        fun createReview_Success() {
            // Given
            val request = RequestCreateReviewDto(
                memberId = 1L, contentId = 10L, rating = 4, status = ReviewStatus.PLAYED, comment = "재밌다"
            )
            val member: Member = mockk(relaxed = true)
            val content: Content = mockk(relaxed = true)
            val review: Review = mockk(relaxed = true)
            val responseDto = ResponseReviewDto(
                id = 1L, memberId = 1L, contentId = 10L,
                rating = 4, status = ReviewStatus.PLAYED, comment = "재밌다",
                source = ReviewSource.MANUAL, createdDt = Instant.now()
            )

            every { reviewRepository.findActiveByMemberAndContent(1L, 10L) } returns null
            every { memberRepository.findById(1L) } returns Optional.of(member)
            every { contentRepository.findById(10L) } returns Optional.of(content)
            every { ReviewMapper.toEntity(request, member, content) } returns review
            every { reviewRepository.save(review) } returns review
            every { ReviewMapper.toDto(review) } returns responseDto

            // When
            val result = reviewService.createReview(request)

            // Then
            assertAll(
                { assertEquals(1L, result.id) },
                { assertEquals(4, result.rating) },
                { assertEquals(ReviewStatus.PLAYED, result.status) },
                { assertEquals("재밌다", result.comment) }
            )
            verify(exactly = 1) { reviewRepository.save(review) }
        }

        @Test
        @DisplayName("동일 Member + Content 중복 평가 - DUPLICATE_REVIEW 예외")
        fun createReview_DuplicateReview() {
            // Given
            val request = RequestCreateReviewDto(
                memberId = 1L, contentId = 10L, rating = 4, status = ReviewStatus.PLAYED
            )
            val existingReview: Review = mockk(relaxed = true)

            every { reviewRepository.findActiveByMemberAndContent(1L, 10L) } returns existingReview

            // When & Then
            val exception = assertThrows(CustomException::class.java) {
                reviewService.createReview(request)
            }
            assertEquals(ErrorCode.DUPLICATE_REVIEW, exception.errorCode)
        }

        @Test
        @DisplayName("rating=0 - INVALID_RATING 예외")
        fun createReview_RatingZero_InvalidRating() {
            // Given
            val request = RequestCreateReviewDto(
                memberId = 1L, contentId = 10L, rating = 0, status = ReviewStatus.PLAYED
            )

            // When & Then
            val exception = assertThrows(CustomException::class.java) {
                reviewService.createReview(request)
            }
            assertEquals(ErrorCode.INVALID_RATING, exception.errorCode)
        }

        @Test
        @DisplayName("rating=6 - INVALID_RATING 예외")
        fun createReview_RatingSix_InvalidRating() {
            // Given
            val request = RequestCreateReviewDto(
                memberId = 1L, contentId = 10L, rating = 6, status = ReviewStatus.PLAYED
            )

            // When & Then
            val exception = assertThrows(CustomException::class.java) {
                reviewService.createReview(request)
            }
            assertEquals(ErrorCode.INVALID_RATING, exception.errorCode)
        }
    }

    @Nested
    @DisplayName("updateReview 테스트")
    inner class UpdateReviewTest {

        @Test
        @DisplayName("rating, status, comment 수정 성공")
        fun updateReview_Success() {
            // Given
            val request = RequestUpdateReviewDto(
                reviewId = 1L, rating = 5, status = ReviewStatus.FAVORITE, comment = "명작"
            )
            val member: Member = mockk(relaxed = true)
            val content: Content = mockk(relaxed = true)
            val review = Review(member = member, content = content, rating = 3, status = ReviewStatus.DROPPED)
            val responseDto = ResponseReviewDto(
                id = 1L, memberId = 1L, contentId = 10L,
                rating = 5, status = ReviewStatus.FAVORITE, comment = "명작",
                source = ReviewSource.MANUAL, createdDt = Instant.now()
            )

            every { reviewRepository.findById(1L) } returns Optional.of(review)
            every { ReviewMapper.toDto(review) } returns responseDto

            // When
            val result = reviewService.updateReview(request)

            // Then
            assertAll(
                { assertEquals(5, review.rating) },
                { assertEquals(ReviewStatus.FAVORITE, review.status) },
                { assertEquals("명작", review.comment) },
                { assertEquals(5, result.rating) }
            )
        }

        @Test
        @DisplayName("존재하지 않는 리뷰 수정 - REVIEW_NOT_FOUND 예외")
        fun updateReview_NotFound() {
            // Given
            val request = RequestUpdateReviewDto(reviewId = 999L, rating = 5)

            every { reviewRepository.findById(999L) } returns Optional.empty()

            // When & Then
            val exception = assertThrows(CustomException::class.java) {
                reviewService.updateReview(request)
            }
            assertEquals(ErrorCode.REVIEW_NOT_FOUND, exception.errorCode)
        }

        @Test
        @DisplayName("rating 수정 시 유효하지 않은 값 - INVALID_RATING 예외")
        fun updateReview_InvalidRating() {
            // Given
            val request = RequestUpdateReviewDto(reviewId = 1L, rating = 0)
            val review: Review = mockk(relaxed = true)

            every { reviewRepository.findById(1L) } returns Optional.of(review)

            // When & Then
            val exception = assertThrows(CustomException::class.java) {
                reviewService.updateReview(request)
            }
            assertEquals(ErrorCode.INVALID_RATING, exception.errorCode)
        }
    }

    @Nested
    @DisplayName("deleteReview 테스트")
    inner class DeleteReviewTest {

        @Test
        @DisplayName("리뷰 삭제 성공 - repository.delete 호출 확인")
        fun deleteReview_Success() {
            // Given
            val reviewId = 1L
            val member: Member = mockk(relaxed = true)
            val content: Content = mockk(relaxed = true)
            val review = Review(member = member, content = content, rating = 4, status = ReviewStatus.PLAYED)

            every { reviewRepository.findById(reviewId) } returns Optional.of(review)
            every { reviewRepository.delete(review) } just Runs

            // When
            reviewService.deleteReview(reviewId)

            // Then
            verify(exactly = 1) { reviewRepository.delete(review) }
        }

        @Test
        @DisplayName("존재하지 않는 리뷰 삭제 - REVIEW_NOT_FOUND 예외")
        fun deleteReview_NotFound() {
            // Given
            val reviewId = 999L

            every { reviewRepository.findById(reviewId) } returns Optional.empty()

            // When & Then
            val exception = assertThrows(CustomException::class.java) {
                reviewService.deleteReview(reviewId)
            }
            assertEquals(ErrorCode.REVIEW_NOT_FOUND, exception.errorCode)
        }
    }
}
