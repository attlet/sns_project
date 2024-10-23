package com.kotlin.sns.domain.Notification.service.Impl

import com.kotlin.sns.common.exception.CustomException
import com.kotlin.sns.common.exception.ExceptionConst
import com.kotlin.sns.domain.Member.entity.Member
import com.kotlin.sns.domain.Member.repository.MemberRepository
import com.kotlin.sns.domain.Notification.dto.request.RequestCreateNotificationDto
import com.kotlin.sns.domain.Notification.dto.response.ResponseNotificationDto
import com.kotlin.sns.domain.Notification.entity.Notification
import com.kotlin.sns.domain.Notification.entity.NotificationType
import com.kotlin.sns.domain.Notification.repository.NotificationRepository
import com.kotlin.sns.domain.Notification.service.NotificationService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 알림 로직
 *
 * @property notificationRepository
 * @property memberRepository
 */
@Service
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val memberRepository: MemberRepository
)  : NotificationService {

    /**
     * 알림 생성 메서드
     * findAllById를 통해 db 한 번만 접근
     *
     * @param receiversId
     * @param senderId
     * @param type
     * @param message
     */
    @Transactional
    override fun createNotification(requestCreateNotificationDto: RequestCreateNotificationDto) {
        val receiversId = requestCreateNotificationDto.receiverId
        val senderId = requestCreateNotificationDto.senderId
        val type = requestCreateNotificationDto.type
        val message = requestCreateNotificationDto.message

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
    @Transactional(readOnly = true)
    override fun getNotificationsForUser(receiverId: Long): List<Notification> {
        val receiver = memberRepository.findById(receiverId)
            .orElseThrow { CustomException(ExceptionConst.MEMBER, HttpStatus.NOT_FOUND, "Receiver not found") }

        return notificationRepository.findAllByReceiver(receiver)
    }
}
