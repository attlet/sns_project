package com.kotlin.sns.config

import com.kotlin.sns.domain.Notification.messageQueue.NotificationRedisConsumer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer

@Configuration
class RedisSubscriberConfig(
    private val redisConnectionFactory: RedisConnectionFactory,
    private val notificationRedisConsumer: NotificationRedisConsumer,
    private val topic: ChannelTopic
) {

    /**
     * connection factory통해 redis 연결
     * 메시지 리스너와 topic연결
     *
     * @return
     */
    @Bean
    fun redisMessageListenerContainer() : RedisMessageListenerContainer{
        val container = RedisMessageListenerContainer()
        container.setConnectionFactory(redisConnectionFactory)
        container.addMessageListener(notificationRedisConsumer, topic)
        return container
    }
}