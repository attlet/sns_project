package com.kotlin.sns.domain.Notification.service

import com.kotlin.sns.domain.Notification.dto.response.ResponseNotificationDto
import com.kotlin.sns.domain.Notification.entity.Notification
import com.kotlin.sns.domain.Notification.entity.NotificationType

interface NotificationService {
    fun createNotification(receiversId: List<Long>, senderId: Long?, type: NotificationType, message: String)
    fun getNotificationsForUser(receiverId: Long): List<Notification>
}