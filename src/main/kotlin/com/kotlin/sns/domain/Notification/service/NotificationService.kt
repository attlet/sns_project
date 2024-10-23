package com.kotlin.sns.domain.Notification.service

import com.kotlin.sns.domain.Notification.dto.request.RequestCreateNotificationDto
import com.kotlin.sns.domain.Notification.dto.response.ResponseNotificationDto
import com.kotlin.sns.domain.Notification.entity.Notification
import com.kotlin.sns.domain.Notification.entity.NotificationType

interface NotificationService {
    fun createNotification(requestCreateNotificationDto: RequestCreateNotificationDto)
    fun getNotificationsForUser(receiverId: Long): List<Notification>
}