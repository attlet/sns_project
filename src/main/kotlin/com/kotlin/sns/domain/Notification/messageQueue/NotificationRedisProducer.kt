package com.kotlin.sns.domain.Notification.messageQueue

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.kotlin.sns.domain.Notification.entity.Notification
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.ChannelTopic

class NotificationRedisProducer(
    private val objectMapper: ObjectMapper,
    private val redisTemplate: RedisTemplate<String, String>,
    private val topic: ChannelTopic
) : NotificationProducer{
    override fun sendNotification(notification: Notification) {
        val message = objectMapper.writeValueAsString(notification) //json 변환
        redisTemplate.convertAndSend(topic.topic, message)
    }
}