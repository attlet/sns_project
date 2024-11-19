package com.kotlin.sns.domain.Notification.messageQueue

import com.kotlin.sns.domain.Notification.entity.Notification

interface NotificationConsumer {
    fun receiveMessage(notification : Notification)
}