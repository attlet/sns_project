package com.kotlin.sns.domain.Notification.messageQueue

import com.kotlin.sns.domain.Notification.dto.request.RequestPublishDto
import com.kotlin.sns.domain.Notification.entity.Notification
import com.kotlin.sns.domain.Notification.repository.SseRepository
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException
@Profile("prod")
@Component
class NotificationRabbitConsumer(
    private val sseRepository: SseRepository
): NotificationConsumer {

    @RabbitListener(queues = ["\${spring.rabbitmq.queue}"])   //rabbitmq의 특정 queue에 데이터가 들어올 때 마다 호출
    override fun receiveMessage(requestPublishDto: RequestPublishDto) {
        val receiverId = requestPublishDto.receiverId
        val emitter = sseRepository.get(receiverId)             //추후 처리하는 로직 작성 필요

        if(emitter != null){
            try{
                emitter.send(
                    SseEmitter
                    .event()
                    .name("notification")
                    .data(requestPublishDto))
            } catch (e : IOException){
                sseRepository.remove(receiverId)   //추후 체크 필요
            }
        } else {
            // null 처리
        }
    }
}