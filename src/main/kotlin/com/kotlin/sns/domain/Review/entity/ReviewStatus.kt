package com.kotlin.sns.domain.Review.entity

/**
 * 작품 평가 상태
 *
 * 사용자가 해당 작품을 어떤 상태로 경험했는지를 나타낸다.
 */
enum class ReviewStatus {
    /** 현재 플레이/시청 중 */
    PLAYING,
    /** 플레이/시청 완료 */
    PLAYED,
    /** 중도 포기 */
    DROPPED,
    /** 관심 목록 (아직 경험하지 않음) */
    WISHLIST,
    /** 최애 작품 */
    FAVORITE
}
