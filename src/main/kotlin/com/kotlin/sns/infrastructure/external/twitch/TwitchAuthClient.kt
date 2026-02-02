package com.kotlin.sns.infrastructure.external.twitch

import com.kotlin.sns.infrastructure.external.twitch.dto.TwitchTokenResponse

/**
 * Twitch OAuth 인증 클라이언트 인터페이스
 *
 * IGDB API 호출을 위한 Twitch OAuth 토큰 발급/관리
 */
interface TwitchAuthClient {

    /**
     * 새로운 액세스 토큰을 발급받습니다.
     *
     * @return TwitchTokenResponse 토큰 정보
     * @throws TwitchAuthException 인증 실패 시
     */
    fun getAccessToken(): TwitchTokenResponse

    /**
     * 현재 유효한 토큰을 반환합니다.
     * 만료된 경우 자동으로 갱신합니다.
     *
     * @return 유효한 액세스 토큰 문자열
     */
    fun getValidToken(): String
}
