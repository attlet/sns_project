package com.kotlin.sns

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
@ConfigurationPropertiesScan("com.kotlin.sns")
class SnsApplication

fun main(args: Array<String>) {
	runApplication<SnsApplication>(*args)
}
