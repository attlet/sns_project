package com.kotlin.sns

import com.kotlin.sns.domain.Review.helper.SteamRatingProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
@EnableConfigurationProperties(SteamRatingProperties::class)
class SnsApplication

fun main(args: Array<String>) {
	runApplication<SnsApplication>(*args)
}
