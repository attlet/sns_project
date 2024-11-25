package com.kotlin.sns.domain.Notification.messageQueue

import com.fasterxml.jackson.databind.ObjectMapper
import com.kotlin.sns.domain.Notification.entity.Notification
import com.kotlin.sns.domain.Notification.repository.SseRepository
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException

@Profile("local")
@Component
class NotificationRedisConsumer(
    private val sseRepository: SseRepository,
    private val objectMapper: ObjectMapper
) : NotificationConsumer, MessageListener{
    override fun onMessage(message: Message, pattern: ByteArray?) {
        val notificationJson = String(message.body)
        val notification = objectMapper.readValue(notificationJson, Notification::class.java)  //json 다시 객체로 변환
        receiveMessage(notification)
    }
    override fun receiveMessage(notification: Notification) {
        val receiverId = notification.receiver.id
        val emitter = sseRepository.get(receiverId)             //추후 처리하는 로직 작성 필요

        if(emitter != null){
            try{
                emitter.send(
                    SseEmitter
                        .event()
                        .name("notification")
                        .data(notification))
            } catch (e : IOException){
                sseRepository.remove(receiverId)   //추후 체크 필요
            }
        } else {
            // null 처리
        }
    }
}