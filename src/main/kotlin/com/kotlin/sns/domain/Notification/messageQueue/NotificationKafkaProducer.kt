package com.kotlin.sns.domain.Notification.messageQueue

import com.kotlin.sns.domain.Notification.entity.Notification
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component


@Component
class NotificationKafkaProducer(
    private val kafkaTemplate: KafkaTemplate<String, Notification>
) : NotificationProducer{
    @Value("\${kafka.topic}")
    private lateinit var topic : String

    override fun sendNotification(notification: Notification) {
        kafkaTemplate.send(topic, notification)
    }
}