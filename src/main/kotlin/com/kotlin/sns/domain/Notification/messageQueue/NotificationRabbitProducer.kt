package com.kotlin.sns.domain.Notification.messageQueue

import com.kotlin.sns.domain.Notification.entity.Notification
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value

class NotificationRabbitProducer(
    private val rabbitTemplate: RabbitTemplate
) : NotificationProducer{

    override fun sendNotification(notification: Notification) {
        rabbitTemplate.convertAndSend(notification)
    }
}