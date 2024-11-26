package com.kotlin.sns.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig {
    @Configuration
    class WebConfig : WebMvcConfigurer {
        override fun configureContentNegotiation(configurer: ContentNegotiationConfigurer) {
            configurer.favorPathExtension(false)
        }
    }
}