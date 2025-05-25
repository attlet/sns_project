package com.kotlin.sns.domain.Sse.ConnectionRegistry.Impl

import com.kotlin.sns.domain.Sse.ConnectionRegistry.ConnectionRegistry
import org.springframework.data.redis.core.RedisTemplate
import java.util.*

/**
 * Redis 연결 레지스트리 구현
 * 이 클래스는 Redis를 사용하여 사용자 ID와 인스턴스 ID 간의 연결을 관리합니다.
 *
 * @property redisTemplate
 */
class RedisConnectionRegistry(
    private val redisTemplate: RedisTemplate<String, String>
) : ConnectionRegistry {
    private val instanceId = UUID.randomUUID().toString()
    private val connectionPrefix = "connection:"

    override fun register(userId: Long, instanceId: String) {
        val key = "$connectionPrefix$userId"
        redisTemplate.opsForValue().set(key, instanceId)
    }

    override fun getManagingInstance(userId: Long): String? {
        val key = "$connectionPrefix$userId"
        return redisTemplate.opsForValue().get(key)
    }

    override fun unregister(userId: Long) {
        val key = "$connectionPrefix$userId"
        redisTemplate.delete(key)
    }

    fun getInstanceId(): String = instanceId
}