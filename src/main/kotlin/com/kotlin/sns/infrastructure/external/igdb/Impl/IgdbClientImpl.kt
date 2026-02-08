package com.kotlin.sns.infrastructure.external.igdb.Impl

import com.kotlin.sns.common.exception.CustomException
import com.kotlin.sns.common.exception.ErrorCode
import com.kotlin.sns.infrastructure.external.igdb.IgdbClient
import com.kotlin.sns.infrastructure.external.igdb.dto.IgdbGameDto
import com.kotlin.sns.infrastructure.external.igdb.dto.IgdbExternalGameResponse
import com.kotlin.sns.infrastructure.external.igdb.dto.IgdbGameResponse
import com.kotlin.sns.infrastructure.external.igdb.dto.IgdbSearchResultDto
import com.kotlin.sns.infrastructure.external.twitch.TwitchAuthClient
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatusCode
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

private val logger = KotlinLogging.logger {}

/**
 * IGDB API 클라이언트 구현체
 *
 * WebClient를 사용하여 IGDB API를 호출합니다.
 * TwitchAuthClient를 통해 인증 토큰을 관리합니다.
 *
 * @property webClient IGDB API용 WebClient 인스턴스
 * @property twitchAuthClient Twitch OAuth 인증 클라이언트
 * @property clientId Twitch Client ID (IGDB 요청 헤더에 필요)
 */
class IgdbClientImpl(
    private val webClient: WebClient,
    private val twitchAuthClient: TwitchAuthClient,
    private val clientId: String
) : IgdbClient {

    companion object {
        private const val GAMES_ENDPOINT = "/games"
        private const val EXTERNAL_GAMES_ENDPOINT = "/external_games"
        private const val STEAM_CATEGORY = 1
        private const val DEFAULT_SEARCH_LIMIT = 10
    }
    override fun searchGames(query: String): List<IgdbSearchResultDto> {
        val token = twitchAuthClient.getValidToken()
        val requestBody = buildSearchQuery(query)

        logger.debug { "IGDB 게임 검색 요청: query=$query" }

        return webClient.post()
            .uri(GAMES_ENDPOINT)
            .header("Client-ID", clientId)
            .header("Authorization", "Bearer $token")
            .bodyValue(requestBody)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError) {
                logger.error { "IGDB API 클라이언트 에러: ${it.statusCode()}" }
                Mono.error(CustomException(ErrorCode.IGDB_API_FAILED))
            }
            .onStatus(HttpStatusCode::is5xxServerError) {
                logger.error { "IGDB API 서버 에러: ${it.statusCode()}" }
                Mono.error(CustomException(ErrorCode.IGDB_API_SERVER_ERROR))
            }
            .bodyToFlux(IgdbGameResponse::class.java)
            .map { it.toSearchResult() }
            .collectList()
            .block() ?: emptyList()
    }

    override fun getGameById(id: Long): IgdbGameDto? {
        val token = twitchAuthClient.getValidToken()
        val requestBody = buildDetailQuery(id)

        logger.debug { "IGDB 게임 상세 조회 요청: id=$id" }

        val results = webClient.post()
            .uri(GAMES_ENDPOINT)
            .header("Client-ID", clientId)
            .header("Authorization", "Bearer $token")
            .bodyValue(requestBody)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError) {
                logger.warn { "IGDB API 클라이언트 에러: ${it.statusCode()}" }
                Mono.error(CustomException(ErrorCode.IGDB_API_FAILED))
            }
            .onStatus(HttpStatusCode::is5xxServerError) {
                logger.error { "IGDB API 서버 에러: ${it.statusCode()}" }
                Mono.error(CustomException(ErrorCode.IGDB_API_SERVER_ERROR))
            }
            .bodyToFlux(IgdbGameResponse::class.java)
            .collectList()
            .block() ?: emptyList()

        return results.firstOrNull()?.toGameDto()
    }

    override fun getGameBySteamAppId(steamAppId: Long): IgdbGameDto? {
        val token = twitchAuthClient.getValidToken()
        val requestBody = buildSteamQuery(steamAppId)

        logger.debug { "IGDB Steam AppID 조회 요청: steamAppId=$steamAppId" }

        val externalGames = webClient.post()
            .uri(EXTERNAL_GAMES_ENDPOINT)
            .header("Client-ID", clientId)
            .header("Authorization", "Bearer $token")
            .bodyValue(requestBody)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError) {
                logger.warn { "IGDB API 클라이언트 에러: ${it.statusCode()}" }
                Mono.error(CustomException(ErrorCode.IGDB_API_FAILED))
            }
            .onStatus(HttpStatusCode::is5xxServerError) {
                logger.error { "IGDB API 서버 에러: ${it.statusCode()}" }
                Mono.error(CustomException(ErrorCode.IGDB_API_SERVER_ERROR))
            }
            .bodyToFlux(IgdbExternalGameResponse::class.java)
            .collectList()
            .block() ?: emptyList()

        val igdbGameId = externalGames.firstOrNull()?.game ?: return null

        return getGameById(igdbGameId)
    }

    /**
     * IGDB 검색 쿼리 문자열을 생성합니다.
     *
     * @param query 검색 키워드
     * @return IGDB API 쿼리 문자열
     */
    private fun buildSearchQuery(query: String): String {
        return """
            search "$query";
            fields id,name,cover.url;
            limit $DEFAULT_SEARCH_LIMIT;
        """.trimIndent()
    }

    /**
     * IGDB 상세 조회 쿼리 문자열을 생성합니다.
     *
     * @param id IGDB 게임 ID
     * @return IGDB API 쿼리 문자열
     */
    private fun buildDetailQuery(id: Long): String {
        return """
            where id = $id;
            fields id,name,summary,first_release_date,cover.url,genres.name;
        """.trimIndent()
    }

    /**
     * Steam AppID로 IGDB external_games 조회 쿼리를 생성합니다.
     *
     * @param steamAppId Steam 게임 AppID
     * @return IGDB API 쿼리 문자열
     */
    private fun buildSteamQuery(steamAppId: Long): String {
        return """
            fields game;
            where category = $STEAM_CATEGORY & uid = "$steamAppId";
        """.trimIndent()
    }
}


