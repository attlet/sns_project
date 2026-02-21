package com.kotlin.sns.domain.Review.dto.request

import com.kotlin.sns.domain.Review.entity.ReviewStatus

/**
 * 리뷰 수정 요청 DTO
 *
 * null이 아닌 필드만 업데이트된다.
 *
 * @property reviewId 수정할 리뷰 ID (필수)
 * @property rating 별점 1~5
 * @property status 작품 상태
 * @property comment 한줄평 (최대 200자)
 */
data class RequestUpdateReviewDto(
    val reviewId: Long,
    val rating: Int? = null,
    val status: ReviewStatus? = null,
    val comment: String? = null
)
