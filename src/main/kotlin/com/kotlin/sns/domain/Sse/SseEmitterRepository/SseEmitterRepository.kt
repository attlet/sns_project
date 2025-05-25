package com.kotlin.sns.domain.Sse.SseEmitterRepository

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

interface SseEmitterRepository {
    fun save(userId: Long, emitter: SseEmitter)
    fun get(userId: Long): SseEmitter?
    fun remove(userId: Long)
}