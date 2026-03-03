package com.kotlin.sns.infrastructure.external.steam.dto

/**
 * Steam 게임 정보 DTO
 *
 * Steam GetOwnedGames API 응답에서 변환된 개별 게임 데이터를 담는다.
 *
 * @property appId Steam App ID
 * @property name 게임 이름
 * @property playtimeMinutes 총 플레이 시간 (분 단위, 0이면 미플레이)
 */
data class SteamGameDto(
    val appId: Int,
    val name: String,
    val playtimeMinutes: Int
)
