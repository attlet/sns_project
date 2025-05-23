package com.kotlin.sns.domain.Notification.messageQueue

import com.kotlin.sns.domain.Notification.entity.Notification
import com.kotlin.sns.domain.Notification.repository.SseRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException

@Profile("prod")
@Component
class NotificationKafkaConsumer(
    private val sseRepository: SseRepository
) {

    @KafkaListener(topics = ["\${kafka.topic}"], groupId = "\${kafka.groupId")
    fun consume(notification: Notification){
        val receiverId = notification.receiver.id
        val emitter = sseRepository.get(receiverId)

        if (emitter != null) {
            try {
                emitter.send(
                    SseEmitter.event()
                        .name("notification")
                        .data(notification))
            } catch (e: IOException) {
                sseRepository.remove(receiverId) // 오류 발생 시 emitter 제거
            }
        }


    }

}