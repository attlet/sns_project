package com.kotlin.sns.domain.Notification.dto.request

import com.kotlin.sns.domain.Notification.entity.NotificationType

data class RequestPublishDto(
    val receiverId : Long,
    val type : NotificationType,
    val message : String
) {
}