package com.kotlin.sns.domain.Notification.dto.response

import com.kotlin.sns.domain.Notification.entity.NotificationType

data class ResponseNotificationDto(
    val type : NotificationType,
    val message : String
) {
}