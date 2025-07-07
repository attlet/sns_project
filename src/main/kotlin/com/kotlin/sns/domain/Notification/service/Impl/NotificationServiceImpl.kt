package com.kotlin.sns.domain.Notification.service.Impl

import com.kotlin.sns.common.exception.CustomException
import com.kotlin.sns.common.exception.ErrorCode
import com.kotlin.sns.domain.Member.repository.MemberRepository
import com.kotlin.sns.domain.Notification.dto.request.RequestCreateNotificationDto
import com.kotlin.sns.domain.Notification.dto.request.RequestPublishDto
import com.kotlin.sns.domain.Notification.entity.Notification
import com.kotlin.sns.domain.Notification.messageQueue.NotificationProducer
import com.kotlin.sns.domain.Notification.repository.NotificationRepository
import com.kotlin.sns.domain.Notification.repository.SseRepository
import com.kotlin.sns.domain.Notification.service.NotificationService
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException

/**
 * 알림 로직
 *
 * @property notificationRepository
 * @property memberRepository
 */
@Service
class NotificationService(
    private val notificationProducer: NotificationProducer,
    private val notificationRepository: NotificationRepository,
    private val memberRepository: MemberRepository,
    private val sseRepository: SseRepository,
    @Value("\${sse.timeout}") private val sseTimeOut : Long = 0
)  : NotificationService {


    /**
     * 알림 생성 메서드
     * db에 알림 내용 저장 후 sse 알림 발송
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

        //알림 받는 사람들에게 sse 알림 발송
        for(publishDto in publishDtos) {
//            sendNotificationToClient(notification.receiver.id, notification)
            notificationProducer.sendNotification(publishDto)
        }

    }

    // 특정 사용자의 알림 목록 조회
    @Transactional(readOnly = true)
    override fun getNotificationsForUser(receiverId: Long): List<Notification> {
        val receiver = memberRepository.findById(receiverId)
            .orElseThrow { CustomException(ErrorCode.MEMBER_NOT_FOUND) }

        return notificationRepository.findAllByReceiver(receiver)
    }

    /**
     * sse 구독 신청
     *
     * @param userId
     * @return
     */
    override fun subscribe(userId : Long) : SseEmitter{
        val emitter = SseEmitter(sseTimeOut)  //60초 연결

        sseRepository.save(userId, emitter)

        sendNotificationToClient(userId, "dummy")
        emitter.onCompletion { sseRepository.remove(userId)}
        emitter.onTimeout{ sseRepository.remove(userId)}
        emitter.onError { sseRepository.remove(userId) }

        return emitter
    }

    /**
     * parameter로 받은 userId를 가진 member에게 notification 전송하는 메서드
     *
     * @param userId
     * @param dummy
     */
    override fun sendNotificationToClient(userId: Long, dummy : String) {
        val emitter = sseRepository.get(userId)             //추후 처리하는 로직 작성 필요

        if(emitter != null){
            try{
                emitter.send(SseEmitter
                    .event()
                    .name("dummy data")
                    .data(dummy))
            } catch (e : IOException){
                sseRepository.remove(userId)   //추후 체크 필요
            }
        } else {
            // null 처리
        }

    }
}
