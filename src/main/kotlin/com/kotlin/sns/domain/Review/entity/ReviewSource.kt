package com.kotlin.sns.domain.Review.entity

/**
 * 평가 데이터 출처
 *
 * Review 데이터가 어디서 유래했는지를 구분한다.
 */
enum class ReviewSource {
    /** 사용자가 직접 입력 */
    MANUAL,
    /** Steam 연동으로 자동 생성 */
    STEAM,
    /** MyAnimeList 연동으로 자동 생성 */
    MAL,
    /** AniList 연동으로 자동 생성 */
    ANILIST
}
