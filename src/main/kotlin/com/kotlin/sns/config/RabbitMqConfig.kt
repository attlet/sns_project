package com.kotlin.sns.config

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.DirectExchange
import org.springframework.amqp.core.Queue
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Exchange

@Configuration
class RabbitMqConfig {

    @Value("\${spring.rabbitmq.exchange}")
    private lateinit var exchangeName: String

    @Value("\${spring.rabbitmq.queue}")
    private lateinit var queueName: String

    @Value("\${spring.rabbitmq.routing_key}")
    private lateinit var routingKey: String

    /**
     * exchange type을 direct로 지정
     *
     * @return
     */
    @Bean
    fun exchange() : Exchange{
        return DirectExchange(exchangeName)
    }

    @Bean
    fun queue(): Queue {
        return Queue(queueName, true) // durable = true
    }

    @Bean
    fun binding(queue: Queue, exchange: DirectExchange): Binding {
        return BindingBuilder.bind(queue).to(exchange).with(routingKey)
    }
}