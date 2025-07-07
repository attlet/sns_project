package com.kotlin.sns.domain.Notification.repository

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

@Component
class RedisSseRepository(
    private val redisTemplate : RedisTemplate<String, Any>,
    private val objectMapper: ObjectMapper
) : SseRepository {

    // Redis를 사용하여 SseEmitter를 저장하는 로직을 구현합니다.
    // 현재는 구현되지 않았지만, 추후 RedisTemplate 등을 사용하여 구현할 수 있습니다.

    private val logger = KotlinLogging.logger {}
    private val serverInstanceId = getServerInstanceId()
    private val localEmitters = ConcurrentHashMap<Long, SseEmitter>()

    override fun save(userId: Long, emitter: SseEmitter) {
        logger.info{ "redisSseRepository save : $userId, emitter : $emitter" }
        localEmitters[userId] = emitter
        redisTemplate.opsForValue().set("sseEmitter:$userId", serverInstanceId, 60, TimeUnit.MINUTES)
    }

    override fun get(userId: Long): SseEmitter? {
        // Redis에서 SseEmitter 조회 로직
        logger.info { "redisSseRepository get : $userId" }
        return localEmitters[userId]
    }

    override fun remove(userId: Long) {
        // Redis에서 SseEmitter 삭제 로직
        logger.info { "redisSseRepository remove : $userId" }
        localEmitters.remove(userId)
        redisTemplate.delete("sseEmitter:$userId")
    }

    override fun contains(userId: Long): Boolean {
        // Redis에 해당 userId의 SseEmitter가 존재하는지 확인하는 로직
        logger.info { "redisSseRepository contains : $userId" }
        return localEmitters.contains(userId)
    }

    /**
     * 서버 인스턴스 ID를 생성하는 메서드
     * redis pub/sub을 사용하여 여러 서버 인스턴스 간의 SseEmitter를 관리하기 위해
     *
     * @return
     */
    fun getServerInstanceId(): String {
        val processId = ProcessHandle.current().pid() // 현재 프로세스 ID를 가져옴
        return "server-instance-$processId" // 프로세스 ID를 기반으로 서버 인스턴스 ID를 생성
    }
}