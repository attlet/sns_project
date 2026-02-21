package com.kotlin.sns.domain.Review.dto.request

import com.kotlin.sns.domain.Review.entity.ReviewStatus

/**
 * 리뷰 생성 요청 DTO
 *
 * @property memberId 평가자 ID (필수)
 * @property contentId 작품 ID (필수)
 * @property rating 별점 1~5 (필수)
 * @property status 작품 상태 (필수)
 * @property comment 한줄평 (선택, 최대 200자)
 */
data class RequestCreateReviewDto(
    val memberId: Long,
    val contentId: Long,
    val rating: Int,
    val status: ReviewStatus,
    val comment: String? = null
)
