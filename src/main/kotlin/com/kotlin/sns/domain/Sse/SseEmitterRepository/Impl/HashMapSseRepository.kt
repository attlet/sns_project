package com.kotlin.sns.domain.Sse.SseEmitterRepository.Impl

import com.kotlin.sns.domain.Sse.SseEmitterRepository.SseEmitterRepository
import org.springframework.stereotype.Repository
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.ConcurrentHashMap

/**
 * Sse emitter 메모리 저장, 조회 등 로직
 * 단일 로컬 환경에서만 사용 가능한 방식
 */
@Repository
class HashMapSseRepository() : SseEmitterRepository {


    private val emitters = ConcurrentHashMap<Long, SseEmitter>()

    override fun save(userId: Long, emitter: SseEmitter) {
        emitters[userId] = emitter
    }

    override fun get(userId: Long): SseEmitter? = emitters[userId]

    override fun remove(userId: Long) {
        emitters.remove(userId)
    }
}