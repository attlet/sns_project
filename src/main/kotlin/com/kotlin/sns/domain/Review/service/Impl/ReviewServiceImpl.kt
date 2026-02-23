package com.kotlin.sns.domain.Review.service.Impl

import com.kotlin.sns.common.exception.CustomException
import com.kotlin.sns.common.exception.ErrorCode
import com.kotlin.sns.domain.Content.repository.ContentRepository
import com.kotlin.sns.domain.Member.repository.MemberRepository
import com.kotlin.sns.domain.Review.dto.request.RequestCreateReviewDto
import com.kotlin.sns.domain.Review.dto.request.RequestUpdateReviewDto
import com.kotlin.sns.domain.Review.dto.response.ResponseReviewDto
import com.kotlin.sns.domain.Review.entity.ReviewStatus
import com.kotlin.sns.domain.Review.mapper.ReviewMapper
import com.kotlin.sns.domain.Review.repository.ReviewRepository
import com.kotlin.sns.domain.Review.service.ReviewService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Review 서비스 구현체
 *
 * 작품 평가(Review) CRUD 비즈니스 로직을 제공한다.
 *
 * @property reviewRepository 리뷰 레포지토리
 * @property memberRepository 회원 레포지토리
 * @property contentRepository 콘텐츠 레포지토리
 */
@Service
class ReviewServiceImpl(
    private val reviewRepository: ReviewRepository,
    private val memberRepository: MemberRepository,
    private val contentRepository: ContentRepository
) : ReviewService {

    /**
     * 리뷰 ID로 단건 조회
     *
     * @param reviewId 리뷰 ID
     * @return 조회된 리뷰 정보
     * @throws CustomException 리뷰가 존재하지 않거나 삭제된 경우
     */
    @Transactional(readOnly = true)
    override fun getReviewById(reviewId: Long): ResponseReviewDto {
        val review = reviewRepository.findActiveById(reviewId)
            ?: throw CustomException(ErrorCode.REVIEW_NOT_FOUND)

        return ReviewMapper.toDto(review)
    }

    /**
     * 특정 Member의 리뷰 목록 조회 (페이징, 상태 필터 선택적)
     *
     * status가 null이면 전체 리뷰를, 지정되면 해당 상태의 리뷰만 반환한다.
     *
     * @param memberId 회원 ID
     * @param status 리뷰 상태 필터 (null이면 전체 조회)
     * @param pageable 페이징 정보
     * @return 페이징된 리뷰 목록
     */
    @Transactional(readOnly = true)
    override fun getReviewsByMember(memberId: Long, status: ReviewStatus?, pageable: Pageable): Page<ResponseReviewDto> {
        return reviewRepository.findReviewsByMember(memberId, status, pageable)
            .map { ReviewMapper.toDto(it) }
    }

    /**
     * 특정 Content의 리뷰 목록 조회 (페이징)
     *
     * @param contentId 콘텐츠 ID
     * @param pageable 페이징 정보
     * @return 페이징된 리뷰 목록
     */
    @Transactional(readOnly = true)
    override fun getReviewsByContent(contentId: Long, pageable: Pageable): Page<ResponseReviewDto> {
        return reviewRepository.findReviewsByContent(contentId, pageable)
            .map { ReviewMapper.toDto(it) }
    }

    /**
     * 리뷰 생성
     *
     * rating 유효성 검증 및 중복 평가 방지 후 리뷰를 생성한다.
     *
     * @param request 리뷰 생성 요청 DTO
     * @return 생성된 리뷰 정보
     * @throws CustomException 유효하지 않은 별점(INVALID_RATING), 중복 평가(DUPLICATE_REVIEW),
     *         회원 미존재(MEMBER_NOT_FOUND), 콘텐츠 미존재(CONTENT_NOT_FOUND)
     */
    @Transactional
    override fun createReview(request: RequestCreateReviewDto): ResponseReviewDto {
        validateRating(request.rating)

        val existingReview = reviewRepository.findActiveByMemberAndContent(request.memberId, request.contentId)
        if (existingReview != null) {
            throw CustomException(ErrorCode.DUPLICATE_REVIEW)
        }

        val member = memberRepository.findById(request.memberId)
            .orElseThrow { CustomException(ErrorCode.MEMBER_NOT_FOUND) }

        val content = contentRepository.findById(request.contentId)
            .filter { !it.isDeleted }
            .orElseThrow { CustomException(ErrorCode.CONTENT_NOT_FOUND) }

        val review = ReviewMapper.toEntity(request, member, content)
        val saved = reviewRepository.save(review)

        return ReviewMapper.toDto(saved)
    }

    /**
     * 리뷰 수정
     *
     * null이 아닌 필드만 업데이트한다.
     *
     * @param request 리뷰 수정 요청 DTO
     * @return 수정된 리뷰 정보
     * @throws CustomException 리뷰 미존재(REVIEW_NOT_FOUND), 유효하지 않은 별점(INVALID_RATING)
     */
    @Transactional
    override fun updateReview(request: RequestUpdateReviewDto): ResponseReviewDto {
        val review = reviewRepository.findActiveById(request.reviewId)
            ?: throw CustomException(ErrorCode.REVIEW_NOT_FOUND)

        request.rating?.let {
            validateRating(it)
            review.rating = it
        }
        request.status?.let { review.status = it }
        request.comment?.let { review.comment = it }

        return ReviewMapper.toDto(review)
    }

    /**
     * 리뷰 삭제 (Soft Delete)
     *
     * 실제로 데이터를 삭제하지 않고 isDeleted 플래그를 true로 변경한다.
     *
     * @param reviewId 삭제할 리뷰 ID
     * @throws CustomException 리뷰가 존재하지 않거나 이미 삭제된 경우
     */
    @Transactional
    override fun deleteReview(reviewId: Long) {
        val review = reviewRepository.findActiveById(reviewId)
            ?: throw CustomException(ErrorCode.REVIEW_NOT_FOUND)

        review.isDeleted = true
    }

    /**
     * rating 유효성 검증 (1~5 범위)
     *
     * @param rating 검증할 별점 값
     * @throws CustomException 별점이 1~5 범위 밖인 경우
     */
    private fun validateRating(rating: Int) {
        if (rating < 1 || rating > 5) {
            throw CustomException(ErrorCode.INVALID_RATING)
        }
    }
}
