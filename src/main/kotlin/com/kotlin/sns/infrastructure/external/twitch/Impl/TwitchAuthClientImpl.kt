package com.kotlin.sns.infrastructure.external.twitch.Impl

import com.kotlin.sns.infrastructure.external.twitch.TwitchAuthClient
import com.kotlin.sns.infrastructure.external.twitch.dto.TwitchTokenResponse
import com.kotlin.sns.infrastructure.external.twitch.exception.TwitchAuthException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.time.Instant

private val logger = KotlinLogging.logger {}

/**
 * Twitch OAuth 인증 클라이언트 구현체
 *
 * Twitch API를 통해 OAuth 토큰을 발급받고 관리합니다.
 * 토큰 캐싱 및 자동 갱신 기능을 제공합니다.
 *
 * @property webClient WebClient 인스턴스
 * @property clientId Twitch Client ID
 * @property clientSecret Twitch Client Secret
 */
class TwitchAuthClientImpl(
    private val webClient: WebClient,
    private val clientId: String,
    private val clientSecret: String
) : TwitchAuthClient {

    private var cachedToken: String? = null
    private var tokenExpiresAt: Instant? = null

    companion object {
        private const val TOKEN_ENDPOINT = "/oauth2/token"
        private const val GRANT_TYPE = "client_credentials"
        private const val TOKEN_EXPIRY_BUFFER_SECONDS = 60L
    }

    /**
     * 새로운 액세스 토큰을 발급받습니다.
     *
     * @return TwitchTokenResponse 토큰 정보
     * @throws TwitchAuthException 인증 실패 시
     */
    override fun getAccessToken(): TwitchTokenResponse {
        logger.debug { "Requesting new Twitch access token" }

        try {
            //1. WebClient를 통해 twitch Oauth 연동
            val response = webClient.post()
                .uri(TOKEN_ENDPOINT)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(
                    BodyInserters.fromFormData("client_id", clientId)    // 요청 body 설정. form 데이터로 변환됨.
                        .with("client_secret", clientSecret)
                        .with("grant_type", GRANT_TYPE)
                )
                .retrieve()                                                    // 요청 실행 및 응답 처리 시작. 4xx/5xx 결과면 WebClientResponseException 발생
                .bodyToMono(TwitchTokenResponse::class.java)                   // 응답 body를 TwitchTokenResponse로 역직렬화
                .block()
                ?: throw TwitchAuthException("Empty response from Twitch OAuth server")

            // 캐시 업데이트
            cachedToken = response.accessToken
            tokenExpiresAt = response.expiresAt()

            logger.info { "Successfully obtained Twitch access token, expires at: $tokenExpiresAt" }

            return response

        } catch (e: WebClientResponseException) {
            logger.error(e) { "Failed to obtain Twitch access token: ${e.statusCode} - ${e.responseBodyAsString}" }
            throw TwitchAuthException(
                "Failed to obtain Twitch access token: ${e.statusCode.value()} - ${e.responseBodyAsString}",
                e
            )
        } catch (e: TwitchAuthException) {
            throw e
        } catch (e: Exception) {
            logger.error(e) { "Unexpected error while obtaining Twitch access token" }
            throw TwitchAuthException("Unexpected error while obtaining Twitch access token: ${e.message}", e)
        }
    }

    /**
     * 현재 유효한 토큰을 반환합니다.
     * 만료된 경우 자동으로 갱신합니다.
     *
     * @return 유효한 액세스 토큰 문자열
     */
    override fun getValidToken(): String {
        val token = cachedToken
        val expiresAt = tokenExpiresAt

        // 토큰이 없거나 만료 임박 시 새로 발급
        if (token == null || expiresAt == null || isTokenExpiringSoon(expiresAt)) {
            logger.debug { "Token is null or expiring soon, fetching new token" }
            return getAccessToken().accessToken
        }

        logger.debug { "Using cached token, expires at: $expiresAt" }
        return token
    }

    /**
     * 토큰이 곧 만료되는지 확인합니다.
     * 만료 60초 전부터 true를 반환합니다.
     */
    private fun isTokenExpiringSoon(expiresAt: Instant): Boolean {
        return Instant.now().plusSeconds(TOKEN_EXPIRY_BUFFER_SECONDS).isAfter(expiresAt)
    }
}
