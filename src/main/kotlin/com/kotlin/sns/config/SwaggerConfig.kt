package com.kotlin.sns.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {
    @Bean
    fun openApi() : OpenAPI {
        return OpenAPI().info(
            Info().title("sns 사이드 프로젝트")
                .description("sns 사이드 프로젝트 api 명세서")
                .version("v0.1.1")
        )
    }
}