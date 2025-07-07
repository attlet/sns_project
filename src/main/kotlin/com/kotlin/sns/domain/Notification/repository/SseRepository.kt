package com.kotlin.sns.domain.Notification.repository

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

/**
 * Sse emitter 저장소 인터페이스
 *
 * SSE 연결된 클라이언트들을 저장, 조회, 삭제하는 메서드를 정의
 */
interface SseRepository {
    fun save(userId: Long, emitter: SseEmitter)
    fun get(userId: Long): SseEmitter?
    fun remove(userId: Long)
    fun contains(userId: Long): Boolean
}