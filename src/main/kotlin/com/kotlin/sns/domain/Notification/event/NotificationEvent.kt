package com.kotlin.sns.domain.Notification.event

import com.kotlin.sns.domain.Notification.entity.NotificationType

/**
 * 알림 이벤트 클래스
 * 알림 발송에 필요한 데이터를 의미
 *
 * @property receiverId
 * @property senderId
 * @property type
 * @property message
 * @property friendId
 */
data class NotificationEvent(
    val receiverId: List<Long>,
    val senderId: Long?,
    val type: NotificationType,
    val message: String,
    val friendId: Long? = null
)
