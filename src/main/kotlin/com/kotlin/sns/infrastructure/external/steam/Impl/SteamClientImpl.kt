package com.kotlin.sns.infrastructure.external.steam.Impl

import com.kotlin.sns.common.exception.CustomException
import com.kotlin.sns.common.exception.ErrorCode
import com.kotlin.sns.infrastructure.external.steam.SteamClient
import com.kotlin.sns.infrastructure.external.steam.dto.SteamGameDto
import com.kotlin.sns.infrastructure.external.steam.dto.SteamOwnedGamesResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

/**
 * Steam Web API 클라이언트 구현체
 *
 * WebClient를 사용하여 Steam GetOwnedGames API를 호출한다.
 * 4xx/5xx 응답은 모두 STEAM_API_FAILED 예외로 변환한다.
 *
 * 동기화 시, 사용자가 보유한 steam 게임은 content로, steam 리뷰들은 review로 매핑된다.
 *
 * @property webClient HTTP 클라이언트
 * @property apiKey Steam Web API Key
 */
@Component
class SteamClientImpl(
    private val webClient: WebClient,
    @Value("\${external.steam.api-key}") private val apiKey: String
) : SteamClient {

    companion object {
        private const val OWNED_GAMES_PATH = "/IPlayerService/GetOwnedGames/v1/"
    }

    /**
     * Steam 사용자가 보유한 게임 목록을 조회한다.
     *
     * @param steamId Steam 64비트 사용자 ID
     * @return 보유 게임 목록 (appId, name, playtimeMinutes)
     * @throws CustomException API 오류 시 STEAM_API_FAILED
     */
    override fun getOwnedGames(steamId: String): List<SteamGameDto> {
        val response = webClient.get()
            .uri { uriBuilder ->
                uriBuilder.path(OWNED_GAMES_PATH)
                    .queryParam("key", apiKey)
                    .queryParam("steamid", steamId)
                    .queryParam("include_appinfo", 1)
                    .queryParam("include_played_free_games", 1)
                    .queryParam("format", "json")
                    .build()
            }
            .retrieve()
            .onStatus({ it.isError }) {
                throw CustomException(ErrorCode.STEAM_API_FAILED)
            }
            .bodyToMono(SteamOwnedGamesResponse::class.java)
            .block()
            ?: return emptyList()

        return response.toGameDtos()
    }
}
