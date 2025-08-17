package com.kotlin.sns.domain.Notification.service

import com.kotlin.sns.domain.Notification.dto.request.RequestPublishDto
import com.kotlin.sns.domain.Notification.messageQueue.NotificationProducer
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class NotificationSender(
    private val notificationProducer: NotificationProducer
) {
    @Async
    @Transactional
    fun sendNotificationsAsync(publishDtos: List<RequestPublishDto>) {
        for (publishDto in publishDtos) {
            notificationProducer.sendNotification(publishDto)
        }
    }
}
