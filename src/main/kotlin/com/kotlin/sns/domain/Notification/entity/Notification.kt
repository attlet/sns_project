package com.kotlin.sns.domain.Notification.entity

import com.kotlin.sns.common.entity.BaseEntity
import com.kotlin.sns.domain.Member.entity.Member
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
data class Notification(

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    var receiver: Member, // 알림을 받는 사용자

    @ManyToOne
    @JoinColumn(name = "sender_id")
    var sender: Member?, // 알림을 발생시킨 사용자 (친구 신청 등에서 사용)

    @Enumerated(EnumType.STRING)
    var type: NotificationType, // 알림의 유형 (친구 신청, 포스팅 등)

    var message: String, // 알림 메시지

    var isRead: Boolean = false, // 알림을 읽었는지 여부

) : BaseEntity()

enum class NotificationType {
    FRIEND_REQUEST,
    Friend_RESPONSE,
    NEW_POST
}