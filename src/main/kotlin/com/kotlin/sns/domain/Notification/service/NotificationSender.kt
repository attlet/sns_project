package com.kotlin.sns.domain.Notification.service

import com.kotlin.sns.domain.Notification.dto.request.RequestPublishDto
import com.kotlin.sns.domain.Notification.messageQueue.NotificationProducer
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class NotificationSender(
    private val notificationProducer: NotificationProducer
) {
    /**
     * message queue로 알림 전송
     * - @Async로 비동기 처리, 호출한 스레드와 별도의 스레드에서 실행
     *
     * @param publishDtos
     */
    @Async
    @Transactional
    fun sendNotificationsAsync(publishDtos: List<RequestPublishDto>) {
        for (publishDto in publishDtos) {
            notificationProducer.sendNotification(publishDto)
        }
    }
}
