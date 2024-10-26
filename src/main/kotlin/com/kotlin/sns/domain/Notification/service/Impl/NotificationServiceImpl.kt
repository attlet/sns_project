package com.kotlin.sns.domain.Notification.service.Impl

import com.kotlin.sns.common.exception.CustomException
import com.kotlin.sns.common.exception.ExceptionConst
import com.kotlin.sns.domain.Member.repository.MemberRepository
import com.kotlin.sns.domain.Notification.dto.request.RequestCreateNotificationDto
import com.kotlin.sns.domain.Notification.entity.Notification
import com.kotlin.sns.domain.Notification.repository.NotificationRepository
import com.kotlin.sns.domain.Notification.service.NotificationService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

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
     * 연결된 클라이언트들을 저장하는 자료구조
     * 추후 고도화 필요
     */
    private val emittersMap = ConcurrentHashMap<Long, SseEmitter>()

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

        val sender = senderId?.let {
            memberRepository.findById(it)
                .orElseThrow { CustomException(ExceptionConst.MEMBER, HttpStatus.NOT_FOUND, "Sender not found") }
        }

        val receivers = memberRepository.findAllById(receiversId)

        for (receiver in receivers) {
            notifications.add(Notification(
                receiver = receiver,
                sender = sender,
                type = type,
                message = message
            ))
        }

        notificationRepository.saveAll(notifications)

        //알림 받는 사람들에게 sse 알림 발송
        for(notification in notifications) {
            sendNotificationToClient(notification.receiver.id, notification)
        }

    }

    // 특정 사용자의 알림 목록 조회
    @Transactional(readOnly = true)
    override fun getNotificationsForUser(receiverId: Long): List<Notification> {
        val receiver = memberRepository.findById(receiverId)
            .orElseThrow { CustomException(ExceptionConst.MEMBER, HttpStatus.NOT_FOUND, "Receiver not found") }

        return notificationRepository.findAllByReceiver(receiver)
    }

    /**
     * sse 구독 신청
     *
     * @param userId
     * @return
     */
    override fun subscribe(userId : Long) : SseEmitter{
        val emitter = SseEmitter(60 * 1000L)  //60초 연결

        emittersMap[userId] = emitter

        emitter.onCompletion { emittersMap.remove(userId)}
        emitter.onTimeout{ emittersMap.remove(userId)}
        emitter.onError { emittersMap.remove(userId) }

        return emitter
    }

    /**
     * parameter로 받은 userId를 가진 member에게 notification 전송하는 메서드
     *
     * @param userId
     * @param notification
     */
    override fun sendNotificationToClient(userId: Long, notification: Notification) {
        val emitter = emittersMap[userId] ?: return  //추후 처리하는 로직 작성 필요

        try{
            emitter.send(SseEmitter
                .event()
                .name("notification")
                .data(notification))
        } catch (e : IOException){
            emittersMap.remove(userId)   //추후 체크 필요
        }
    }
}
