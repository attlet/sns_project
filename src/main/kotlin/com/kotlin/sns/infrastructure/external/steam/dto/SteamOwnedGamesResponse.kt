package com.kotlin.sns.infrastructure.external.steam.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Steam GetOwnedGames API 원본 응답 DTO
 *
 * Steam Web API의 IPlayerService/GetOwnedGames/v1/ 응답 구조를 매핑한다.
 */
data class SteamOwnedGamesResponse(
    val response: ResponseBody?
) {
    data class ResponseBody(
        @JsonProperty("game_count") val gameCount: Int = 0,
        val games: List<GameEntry> = emptyList()
    )

    data class GameEntry(
        @JsonProperty("appid") val appId: Int,
        val name: String,
        @JsonProperty("playtime_forever") val playtimeForever: Int
    )

    /**
     * 원본 응답을 서비스 계층용 SteamGameDto 목록으로 변환한다.
     */
    fun toGameDtos(): List<SteamGameDto> {
        return response?.games?.map { entry ->
            SteamGameDto(
                appId = entry.appId,
                name = entry.name,
                playtimeMinutes = entry.playtimeForever
            )
        } ?: emptyList()
    }
}
