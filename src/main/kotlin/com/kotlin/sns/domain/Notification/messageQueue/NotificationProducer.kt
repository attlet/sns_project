package com.kotlin.sns.domain.Notification.messageQueue

import com.kotlin.sns.domain.Notification.dto.request.RequestPublishDto
import com.kotlin.sns.domain.Notification.entity.Notification

interface NotificationProducer {
    fun sendNotification(requestPublishDto: RequestPublishDto)
}