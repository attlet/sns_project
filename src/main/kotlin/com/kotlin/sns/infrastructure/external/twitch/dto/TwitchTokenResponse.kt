package com.kotlin.sns.infrastructure.external.twitch.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

/**
 * Twitch OAuth 토큰 응답 DTO
 *
 * @property accessToken 발급된 액세스 토큰
 * @property expiresIn 토큰 만료까지 남은 시간(초)
 * @property tokenType 토큰 타입 (bearer)
 */
data class TwitchTokenResponse(
    @JsonProperty("access_token")
    val accessToken: String,

    @JsonProperty("expires_in")
    val expiresIn: Long,

    @JsonProperty("token_type")
    val tokenType: String
) {
    /**
     * 토큰 만료 시간 계산
     */
    fun expiresAt(): Instant = Instant.now().plusSeconds(expiresIn)
}
