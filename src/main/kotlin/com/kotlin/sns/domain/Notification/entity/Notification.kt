package com.kotlin.sns.domain.Notification.entity

import com.kotlin.sns.common.entity.BaseEntity
import com.kotlin.sns.domain.Member.entity.Member
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
data class Notification(

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    val receiver: Member, // 알림을 받는 사용자

    @ManyToOne
    @JoinColumn(name = "sender_id")
    val sender: Member?, // 알림을 발생시킨 사용자 (친구 신청 등에서 사용)

    @Enumerated(EnumType.STRING)
    val type: NotificationType, // 알림의 유형 (친구 신청, 포스팅 등)

    val message: String, // 알림 메시지

    val read: Boolean = false, // 알림을 읽었는지 여부

    val createdAt: LocalDateTime = LocalDateTime.now()
) : BaseEntity()

enum class NotificationType {
    FRIEND_REQUEST,
    NEW_POST
}