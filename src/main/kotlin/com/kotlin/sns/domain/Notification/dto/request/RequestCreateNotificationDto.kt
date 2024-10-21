package com.kotlin.sns.domain.Notification.dto.request

import com.kotlin.sns.domain.Notification.entity.NotificationType

data class RequestCreateNotificationDto (
    val receiverId : List<Long>,
    val senderId : Long?,
    val type : NotificationType,
    val message : String,
){
}