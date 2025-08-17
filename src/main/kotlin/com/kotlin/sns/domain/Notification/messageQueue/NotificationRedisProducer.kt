package com.kotlin.sns.domain.Notification.messageQueue

import com.fasterxml.jackson.databind.ObjectMapper
import com.kotlin.sns.domain.Notification.dto.request.RequestPublishDto
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Profile("local")
@Component
class NotificationRedisProducer(
    private val objectMapper: ObjectMapper,
    private val redisTemplate: RedisTemplate<String, String>,
    private val topic: ChannelTopic
) : NotificationProducer{

    private val logger = KotlinLogging.logger{}

    @Async
    override fun sendNotification(requestPublishDto: RequestPublishDto) {
        val message = objectMapper.writeValueAsString(requestPublishDto) //json 변환
        val cntOfReceiver = redisTemplate.convertAndSend(topic.topic, message)
        logger.debug { "cnt of receiver : $cntOfReceiver , message : $message" }
    }
}