package com.kotlin.sns.domain.Sse.SseEmitterRepository.Impl

import com.kotlin.sns.domain.Sse.ConnectionRegistry.Impl.RedisConnectionRegistry
import com.kotlin.sns.domain.Sse.SseEmitterRepository.SseEmitterRepository
import com.kotlin.sns.domain.Sse.messageBroker.MessageBroker
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@Component
class RedisSseRepository(
    private val hashMapSseRepository: HashMapSseRepository,
    private val connectionRegistry: RedisConnectionRegistry,
    private val messageBroker: MessageBroker
) : SseEmitterRepository {

    private val instanceId = connectionRegistry.getInstanceId()

    override fun save(userId: Long, emitter: SseEmitter) {
        // 로컬 저장소에 emitter 저장
        hashMapSseRepository.save(userId, emitter)

        // Redis에 연결 정보 등록
        connectionRegistry.register(userId, instanceId)

        // 연결 종료 시 정리 작업
        emitter.onCompletion { remove(userId) }
        emitter.onTimeout { remove(userId) }
        emitter.onError { remove(userId) }
    }

    override fun get(userId: Long): SseEmitter? {
        // 이 인스턴스가 해당 사용자 연결을 관리하는 경우에만 반환
        val managingInstance = connectionRegistry.getManagingInstance(userId)
        return if (managingInstance == instanceId) {
            hashMapSseRepository.get(userId)
        } else {
            null
        }
    }

    override fun remove(userId: Long) {
        hashMapSseRepository.remove(userId)
        connectionRegistry.unregister(userId)
    }
}