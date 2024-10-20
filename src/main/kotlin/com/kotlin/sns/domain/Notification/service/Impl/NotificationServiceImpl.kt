package com.kotlin.sns.domain.Notification.service.Impl

import com.kotlin.sns.common.exception.CustomException
import com.kotlin.sns.common.exception.ExceptionConst
import com.kotlin.sns.domain.Member.repository.MemberRepository
import com.kotlin.sns.domain.Notification.entity.Notification
import com.kotlin.sns.domain.Notification.entity.NotificationType
import com.kotlin.sns.domain.Notification.repository.NotificationRepository
import com.kotlin.sns.domain.Notification.service.NotificationService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val memberRepository: MemberRepository
)  : NotificationService {

    // 알림 생성

    override fun createNotification(receiverId: Long, senderId: Long?, type: NotificationType, message: String) {
        val receiver = memberRepository.findById(receiverId)
            .orElseThrow { CustomException(ExceptionConst.MEMBER, HttpStatus.NOT_FOUND, "Receiver not found") }

        val sender = senderId?.let {
            memberRepository.findById(it)
                .orElseThrow { CustomException(ExceptionConst.MEMBER, HttpStatus.NOT_FOUND, "Sender not found") }
        }

        val notification = Notification(
            receiver = receiver,
            sender = sender,
            type = type,
            message = message
        )

        notificationRepository.save(notification)
    }

    // 특정 사용자의 알림 목록 조회

    override fun getNotificationsForUser(receiverId: Long): List<Notification> {
        val receiver = memberRepository.findById(receiverId)
            .orElseThrow { CustomException(ExceptionConst.MEMBER, HttpStatus.NOT_FOUND, "Receiver not found") }

        return notificationRepository.findAllByReceiver(receiver)
    }
}
