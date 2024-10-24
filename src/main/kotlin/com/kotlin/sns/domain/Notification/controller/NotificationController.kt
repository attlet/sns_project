package com.kotlin.sns.domain.Notification.controller

import com.kotlin.sns.domain.Notification.dto.request.RequestCreateNotificationDto
import com.kotlin.sns.domain.Notification.dto.response.ResponseNotificationDto
import com.kotlin.sns.domain.Notification.entity.Notification
import com.kotlin.sns.domain.Notification.service.NotificationService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
@RequestMapping("/notify")
class NotificationController(
    private val notificationService: NotificationService
)
{
    @PostMapping("/make")
    fun createNotification(@RequestBody requestCreateNotificationDto: RequestCreateNotificationDto){
        notificationService.createNotification(requestCreateNotificationDto)
    }

    @GetMapping("/getReceiverNotification")
    fun getNotificationForReceiver(@RequestParam("receiverId") receiverId : Long) : List<ResponseNotificationDto>{
        val notifications = notificationService.getNotificationsForUser(receiverId)
        val responseNotificationDtos = mutableListOf<ResponseNotificationDto>()

        for (notification in notifications) {
            responseNotificationDtos.add(ResponseNotificationDto(
                type = notification.type,
                message = notification.message
            ))
        }

        return responseNotificationDtos
    }

//    @GetMapping("/connect", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
//    fun SseConnect() : SseEmitter{
//
//    }

}