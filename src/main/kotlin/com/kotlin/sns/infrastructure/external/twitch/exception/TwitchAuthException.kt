package com.kotlin.sns.infrastructure.external.twitch.exception

/**
 * Twitch OAuth 인증 관련 예외
 */
class TwitchAuthException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)
