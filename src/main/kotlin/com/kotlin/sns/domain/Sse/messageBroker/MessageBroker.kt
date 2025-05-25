package com.kotlin.sns.domain.Sse.messageBroker

import com.kotlin.sns.domain.Notification.dto.request.RequestPublishDto

interface MessageBroker {
    fun publish(topic: String, requestPublishDto: RequestPublishDto)
    fun subscribe(topic: String, handler: (Any) -> Unit)
}