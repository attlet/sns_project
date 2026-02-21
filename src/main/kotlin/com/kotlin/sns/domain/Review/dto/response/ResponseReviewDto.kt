package com.kotlin.sns.domain.Review.dto.response

import com.kotlin.sns.domain.Review.entity.ReviewSource
import com.kotlin.sns.domain.Review.entity.ReviewStatus
import java.time.Instant

/**
 * 리뷰 응답 DTO
 *
 * @property id 리뷰 ID
 * @property memberId 평가자 ID
 * @property contentId 작품 ID
 * @property rating 별점 (1~5)
 * @property status 작품 상태
 * @property comment 한줄평
 * @property source 데이터 출처
 * @property createdDt 생성일시
 */
data class ResponseReviewDto(
    val id: Long,
    val memberId: Long,
    val contentId: Long,
    val rating: Int,
    val status: ReviewStatus,
    val comment: String?,
    val source: ReviewSource,
    val createdDt: Instant
)
