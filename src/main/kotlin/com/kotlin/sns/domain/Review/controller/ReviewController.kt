package com.kotlin.sns.domain.Review.controller

import com.kotlin.sns.domain.Review.dto.request.RequestCreateReviewDto
import com.kotlin.sns.domain.Review.dto.request.RequestUpdateReviewDto
import com.kotlin.sns.domain.Review.dto.response.ResponseReviewDto
import com.kotlin.sns.domain.Review.entity.ReviewStatus
import com.kotlin.sns.domain.Review.service.ReviewService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * Review 컨트롤러
 *
 * 작품 평가(Review) 관련 REST API를 제공한다.
 *
 * @property reviewService 리뷰 서비스
 */
@RestController
@Tag(name = "Review", description = "작품 평가 API")
class ReviewController(
    private val reviewService: ReviewService
) {

    /**
     * 리뷰 단건 조회
     *
     * @param reviewId 조회할 리뷰 ID
     * @return 조회된 리뷰 정보
     */
    @GetMapping("/reviews/{reviewId}")
    @Operation(summary = "리뷰 단건 조회")
    fun getReview(@PathVariable reviewId: Long): ResponseReviewDto {
        return reviewService.getReviewById(reviewId)
    }

    /**
     * 리뷰 생성
     *
     * @param request 리뷰 생성 요청 DTO
     * @return 생성된 리뷰 정보
     */
    @PostMapping("/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "리뷰 생성")
    fun createReview(@RequestBody request: RequestCreateReviewDto): ResponseReviewDto {
        return reviewService.createReview(request)
    }

    /**
     * 리뷰 수정 (rating, status, comment)
     *
     * Path variable의 reviewId를 우선 사용한다.
     *
     * @param reviewId 수정할 리뷰 ID
     * @param request 리뷰 수정 요청 DTO
     * @return 수정된 리뷰 정보
     */
    @PutMapping("/reviews/{reviewId}")
    @Operation(summary = "리뷰 수정")
    fun updateReview(
        @PathVariable reviewId: Long,
        @RequestBody request: RequestUpdateReviewDto
    ): ResponseReviewDto {
        return reviewService.updateReview(request.copy(reviewId = reviewId))
    }

    /**
     * 리뷰 삭제 (Soft Delete)
     *
     * @param reviewId 삭제할 리뷰 ID
     */
    @DeleteMapping("/reviews/{reviewId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "리뷰 삭제 (Soft Delete)")
    fun deleteReview(@PathVariable reviewId: Long) {
        reviewService.deleteReview(reviewId)
    }

    /**
     * 특정 Member의 리뷰 목록 조회 (페이징, 상태별 필터 선택적)
     *
     * @param memberId 회원 ID
     * @param status 리뷰 상태 필터 (null이면 전체 조회)
     * @param page 페이지 번호 (0부터 시작, 기본값: 0)
     * @param size 페이지 크기 (기본값: 10)
     * @return 페이징된 리뷰 목록
     */
    @GetMapping("/members/{memberId}/reviews")
    @Operation(summary = "내 평가 목록 조회 (페이징, 상태 필터)")
    fun getReviewsByMember(
        @PathVariable memberId: Long,
        @RequestParam(required = false) status: ReviewStatus?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): Page<ResponseReviewDto> {
        return reviewService.getReviewsByMember(memberId, status, PageRequest.of(page, size))
    }

    /**
     * 특정 Content의 리뷰 목록 조회 (페이징)
     *
     * @param contentId 콘텐츠 ID
     * @param page 페이지 번호 (0부터 시작, 기본값: 0)
     * @param size 페이지 크기 (기본값: 10)
     * @return 페이징된 리뷰 목록
     */
    @GetMapping("/contents/{contentId}/reviews")
    @Operation(summary = "Content별 리뷰 목록 조회 (페이징)")
    fun getReviewsByContent(
        @PathVariable contentId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): Page<ResponseReviewDto> {
        return reviewService.getReviewsByContent(contentId, PageRequest.of(page, size))
    }
}
