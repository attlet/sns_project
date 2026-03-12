package com.kotlin.sns.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

/**
 * 외부 API 클라이언트 빈 설정
 *
 * 외부 연동에 사용하는 WebClient 빈을 등록한다.
 */
@Configuration
class ExternalClientConfig {

    /**
     * Steam Web API용 WebClient
     *
     * `external.steam.base-url` 프로퍼티로 베이스 URL을 주입받는다.
     *
     * @param baseUrl Steam Web API 베이스 URL (예: https://api.steampowered.com)
     */
    @Bean
    fun webClient(@Value("\${external.steam.base-url}") baseUrl: String): WebClient =
        WebClient.builder()
            .baseUrl(baseUrl)
            .build()
}
