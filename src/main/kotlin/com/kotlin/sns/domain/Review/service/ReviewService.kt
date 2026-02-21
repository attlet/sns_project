package com.kotlin.sns.domain.Review.service

import com.kotlin.sns.domain.Review.dto.request.RequestCreateReviewDto
import com.kotlin.sns.domain.Review.dto.request.RequestUpdateReviewDto
import com.kotlin.sns.domain.Review.dto.response.ResponseReviewDto
import com.kotlin.sns.domain.Review.entity.ReviewStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

/**
 * Review 도메인 서비스 인터페이스
 *
 * 작품 평가(Review) 관련 비즈니스 로직을 정의한다.
 */
interface ReviewService {

    /**
     * 리뷰 ID로 단건 조회
     *
     * @param reviewId 리뷰 ID
     * @return 조회된 리뷰 정보
     * @throws CustomException 리뷰가 존재하지 않을 경우
     */
    fun getReviewById(reviewId: Long): ResponseReviewDto

    /**
     * 특정 Member의 리뷰 목록 조회 (페이징, 상태 필터 선택적)
     *
     * @param memberId 회원 ID
     * @param status 리뷰 상태 필터 (null이면 전체 조회)
     * @param pageable 페이징 정보
     * @return 페이징된 리뷰 목록
     */
    fun getReviewsByMember(memberId: Long, status: ReviewStatus?, pageable: Pageable): Page<ResponseReviewDto>

    /**
     * 특정 Content의 리뷰 목록 조회 (페이징)
     *
     * @param contentId 콘텐츠 ID
     * @param pageable 페이징 정보
     * @return 페이징된 리뷰 목록
     */
    fun getReviewsByContent(contentId: Long, pageable: Pageable): Page<ResponseReviewDto>

    /**
     * 리뷰 생성
     *
     * @param request 리뷰 생성 요청 DTO
     * @return 생성된 리뷰 정보
     * @throws CustomException 중복 평가(DUPLICATE_REVIEW) 또는 유효하지 않은 별점(INVALID_RATING)
     */
    fun createReview(request: RequestCreateReviewDto): ResponseReviewDto

    /**
     * 리뷰 수정
     *
     * @param request 리뷰 수정 요청 DTO
     * @return 수정된 리뷰 정보
     * @throws CustomException 리뷰가 존재하지 않거나 유효하지 않은 별점
     */
    fun updateReview(request: RequestUpdateReviewDto): ResponseReviewDto

    /**
     * 리뷰 삭제 (Soft Delete)
     *
     * @param reviewId 삭제할 리뷰 ID
     * @throws CustomException 리뷰가 존재하지 않을 경우
     */
    fun deleteReview(reviewId: Long)
}
