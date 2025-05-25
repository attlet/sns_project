package com.kotlin.sns.domain.Sse.messageBroker.Impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.kotlin.sns.domain.Notification.dto.request.RequestPublishDto
import com.kotlin.sns.domain.Sse.messageBroker.MessageBroker
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.stereotype.Component

@Component
class RedisMessageBroker(
    private val redisTemplate: RedisTemplate<String, String>,
    private val objectMapper: ObjectMapper,
    private val topic: ChannelTopic
) : MessageBroker {

    private val logger = KotlinLogging.logger{}
    override fun publish(topic: String, requestPublishDto : RequestPublishDto) {
        val message = objectMapper.writeValueAsString(requestPublishDto) //json 변환
        val cntOfReceiver = redisTemplate.convertAndSend(topic, message)
        logger.debug { "cnt of receiver : $cntOfReceiver , message : $message" }
    }

    override fun subscribe(topic: String, handler: (Any) -> Unit) {
        TODO("Not yet implemented")
    }
}