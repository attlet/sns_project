package com.kotlin.sns.domain.Recommendation.dto.response

import com.kotlin.sns.domain.Content.entity.ContentType

/**
 * 추천 콘텐츠 응답 DTO
 *
 * @property contentId 콘텐츠 ID
 * @property title 콘텐츠 제목
 * @property type 콘텐츠 타입 (GAME, MANGA, ANIME)
 * @property thumbnailUrl 썸네일 이미지 URL
 * @property avgRating 평균 별점
 * @property reviewCount 리뷰 수
 * @property recommendationType 추천 방식 (협업 필터링 / 인기도 기반)
 */
data class ResponseRecommendationDto(
    val contentId: Long,
    val title: String,
    val type: ContentType,
    val thumbnailUrl: String?,
    val avgRating: Double,
    val reviewCount: Int,
    val recommendationType: RecommendationType
)

/**
 * 추천 방식을 구분하는 enum
 */
enum class RecommendationType {
    COLLABORATIVE_FILTERING,
    POPULAR
}
