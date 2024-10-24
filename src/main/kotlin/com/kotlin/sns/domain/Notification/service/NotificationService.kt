package com.kotlin.sns.domain.Notification.service

import com.kotlin.sns.domain.Notification.dto.request.RequestCreateNotificationDto
import com.kotlin.sns.domain.Notification.dto.response.ResponseNotificationDto
import com.kotlin.sns.domain.Notification.entity.Notification
import com.kotlin.sns.domain.Notification.entity.NotificationType
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

interface NotificationService {
    fun createNotification(requestCreateNotificationDto: RequestCreateNotificationDto)
    fun getNotificationsForUser(receiverId: Long): List<Notification>
    fun subscribe(userId : Long) : SseEmitter
    fun sendNotificationToClient(userId: Long, notification: Notification)
}