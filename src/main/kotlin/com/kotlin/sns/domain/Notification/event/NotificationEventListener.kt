package com.kotlin.sns.domain.Notification.event

import com.kotlin.sns.common.exception.CustomException
import com.kotlin.sns.common.exception.ErrorCode
import com.kotlin.sns.domain.Member.repository.MemberRepository
import com.kotlin.sns.domain.Notification.dto.request.RequestPublishDto
import com.kotlin.sns.domain.Notification.entity.Notification
import com.kotlin.sns.domain.Notification.repository.NotificationRepository
import com.kotlin.sns.domain.Notification.service.NotificationSender
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

/**
 * 알림 이벤트 publish 시 처리하는 리스너.
 *
 * @property notificationSender
 * @property notificationRepository
 * @property memberRepository
 */
@Component
class NotificationEventListener(
    private val notificationSender: NotificationSender,
    private val notificationRepository: NotificationRepository,
    private val memberRepository: MemberRepository
) {

    private val logger = KotlinLogging.logger {}

    /**
     * 비동기로 알림 발송 처리
     * 트랜잭션 커밋 후에 실행되도록 설정
     *
     * @param event
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional
    fun handleNotificationEvent(event: NotificationEvent) {
        logger.info { "Notification event received: $event" }

        val receiversId = event.receiverId
        val senderId = event.senderId
        val type = event.type
        val message = event.message

        val notifications = mutableListOf<Notification>()
        val publishDtos = mutableListOf<RequestPublishDto>()

        val sender = senderId?.let {
            memberRepository.findById(it)
                .orElseThrow { CustomException(ErrorCode.MEMBER_NOT_FOUND) }
        }

        val receivers = memberRepository.findAllById(receiversId)

        for (receiver in receivers) {
            notifications.add(Notification(
                receiver = receiver,
                sender = sender,
                type = type,
                message = message
            ))

            publishDtos.add(RequestPublishDto(
                receiverId = receiver.id,
                type = type,
                message = message
            ))
        }

        notificationRepository.saveAll(notifications)
        notificationSender.sendNotificationsAsync(publishDtos)
    }
}
