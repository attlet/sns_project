package com.kotlin.sns.domain.Notification.service.Impl

import com.kotlin.sns.common.exception.CustomException
import com.kotlin.sns.common.exception.ExceptionConst
import com.kotlin.sns.domain.Member.entity.Member
import com.kotlin.sns.domain.Member.repository.MemberRepository
import com.kotlin.sns.domain.Notification.dto.response.ResponseNotificationDto
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

    override fun createNotification(receiversId: List<Long>, senderId: Long?, type: NotificationType, message: String) {

        val sender = senderId?.let {
            memberRepository.findById(it)
                .orElseThrow { CustomException(ExceptionConst.MEMBER, HttpStatus.NOT_FOUND, "Sender not found") }
        }

        val receivers = memberRepository.findAllById(receiversId)

        val notifications = mutableListOf<Notification>()

        for (receiver in receivers) {
            notifications.add(Notification(
                receiver = receiver,
                sender = sender,
                type = type,
                message = message
            ))
        }

        notificationRepository.saveAll(notifications)
    }

    // 특정 사용자의 알림 목록 조회

    override fun getNotificationsForUser(receiverId: Long): List<Notification> {
        val receiver = memberRepository.findById(receiverId)
            .orElseThrow { CustomException(ExceptionConst.MEMBER, HttpStatus.NOT_FOUND, "Receiver not found") }

        return notificationRepository.findAllByReceiver(receiver)
    }
}
