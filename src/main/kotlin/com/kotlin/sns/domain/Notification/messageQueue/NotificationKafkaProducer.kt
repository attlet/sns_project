package com.kotlin.sns.domain.Notification.messageQueue

import com.kotlin.sns.domain.Notification.dto.request.RequestPublishDto
import com.kotlin.sns.domain.Notification.entity.Notification
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service


@Profile("prod")
@Component
class NotificationKafkaProducer(
    private val kafkaTemplate: KafkaTemplate<String, RequestPublishDto>
) : NotificationProducer{
    @Value("\${kafka.topic}")
    private lateinit var topic : String

    override fun sendNotification(requestPublishDto: RequestPublishDto) {
        kafkaTemplate.send(topic, requestPublishDto)
    }
}