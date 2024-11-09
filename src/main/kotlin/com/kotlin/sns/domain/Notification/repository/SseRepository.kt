package com.kotlin.sns.domain.Notification.repository

import org.springframework.stereotype.Repository
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.ConcurrentHashMap

/**
 * Sse emitter 메모리 저장, 조회 등 로직
 *
 */
@Repository
class SseRepository {

    /**
     * 연결된 클라이언트들을 저장하는 자료구조
     * 추후 고도화 필요
     */
    private val emittersMap = ConcurrentHashMap<Long, SseEmitter>()

    fun save(userId: Long, emitter: SseEmitter) {
        emittersMap[userId] = emitter
    }

    fun get(userId: Long): SseEmitter? {
        return emittersMap[userId]
    }

    fun remove(userId: Long) {
        emittersMap.remove(userId)
    }

    fun contains(userId: Long): Boolean {
        return emittersMap.containsKey(userId)
    }

}