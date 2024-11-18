package com.kotlin.sns.domain.Notification.kafka

import com.kotlin.sns.domain.Notification.entity.Notification
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class NotificationKafkaProducer(
    private val kafkaTemplate: KafkaTemplate<String, Notification>
) {
    @Value("\${kafka.topic}")
    private lateinit var topic : String

    fun sendNotification(notification: Notification) {
        kafkaTemplate.send(topic, notification)
    }

}