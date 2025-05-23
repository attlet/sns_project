package com.kotlin.sns.domain.Notification.messageQueue

import com.kotlin.sns.domain.Notification.dto.request.RequestPublishDto
import com.kotlin.sns.domain.Notification.entity.Notification
import org.springframework.amqp.core.Exchange
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service

/**
 * RabbitMq producer
 *
 * exchange : 메시지가 큐에 들어가기 전 거치는 곳, exchange type과 라우팅 key에 따라 어느 메시지 큐에 보낼지 결정
 * exchange type 4가지 존재.
 *
 * direct exchange ( default )  : 메시지가 특정 라우팅 key와 일치하는 큐로 전달
 * fanout exchange : 메시지를 라우팅 key를 무시하고 바인딩된 모든 큐로 전달
 * topic exchange : 메시지가 특정 패턴과 일치하는 큐로 전달
 * headers exchange : 메시지의 헤더 값에 따라 맞는 큐에 전달
 *
 * @property rabbitTemplate
 */

@Profile("prod")
@Component
class NotificationRabbitProducer(
    private val rabbitTemplate: RabbitTemplate,
    private val exchange : Exchange
) : NotificationProducer{


    @Value("\${spring.rabbitmq.routing-key}")
    private lateinit var routingKey: String

    override fun sendNotification(requestPublishDto: RequestPublishDto) {
        rabbitTemplate.convertAndSend(exchange.name, routingKey, requestPublishDto)
    }
}