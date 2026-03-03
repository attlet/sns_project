package com.kotlin.sns.domain.Review.dto.response

/**
 * Steam 라이브러리 동기화 결과 DTO
 *
 * @property synced 전체 처리된 게임 수
 * @property created 신규 생성된 Review 수
 * @property updated 업데이트된 Review 수
 */
data class SteamSyncResultDto(
    val synced: Int,
    val created: Int,
    val updated: Int
)
