package com.kotlin.sns.config

import com.kotlin.sns.domain.Notification.messageQueue.NotificationRedisConsumer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig(
    @Value("\${redis.host}") private val host : String,
    @Value("\${redis.port}") private val port : Int
) {

    @Bean
    fun redisConnectionFactory() : RedisConnectionFactory {
        val redisStandaloneConfiguration = RedisStandaloneConfiguration();
        redisStandaloneConfiguration.hostName = host
        redisStandaloneConfiguration.port = port
        return LettuceConnectionFactory(redisStandaloneConfiguration);   //Lettuce vs Jedis 중 lettuce를 선택.
    }

    /**
     * redisTemplate : redis와 통신할 때 사용되는 핵심적인 객체
     * redisConnectionFactory : redis 서버와 연결을 설정하는 팩토리 객체
     *
     * @param connectionFactory
     * @return
     */
    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, String> {
        val template = RedisTemplate<String, String>()
        template.setConnectionFactory(connectionFactory)  //redis 서버와 연결하기 위해 connectionFactory를 지정
        template.keySerializer = StringRedisSerializer()  //redis 에 저장할 데이터의 key 값을 직렬화하는 방법을 설정
        template.valueSerializer = StringRedisSerializer() //redis에 저장할 데이터의 value 값을 직렬화하는 방법을 설정
        return template
    }

    /**
     * Redis Pub/Sub에서 채널 이름을 정의.
     * publicsher와  consumer가 동일한 채널 이름 사용 시 메시지 주고 받기 가능
     *
     * @return
     */
    @Bean
    fun topic(): ChannelTopic{
        return ChannelTopic("notification_channel")  //채널 이름을 설정
    }

}