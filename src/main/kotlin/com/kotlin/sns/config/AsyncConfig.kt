package com.kotlin.sns.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor
import java.util.concurrent.ThreadPoolExecutor

/**
 * 비동기 메서드 실행을 위한 스레드 풀 설정
 * - @Async 어노테이션이 붙은 메서드를 실행할 때 사용
 *
 */
@Configuration
@EnableAsync
@Profile("!test")
class AsyncConfig {

    @Bean(name = ["taskExecutor"])
    fun taskExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        // 사용 가능한 프로세서 수에 기반하여 스레드 수 설정
        val processors = Runtime.getRuntime().availableProcessors()
        executor.corePoolSize = processors
        executor.maxPoolSize = processors * 2
        executor.queueCapacity = 50 // 50개까지만 대기 허용
        // 대기 큐가 꽉 차면 새로 들어온 작업을 버림
        executor.setRejectedExecutionHandler(ThreadPoolExecutor.DiscardPolicy())
        executor.setThreadNamePrefix("Async-")
        executor.initialize()
        return executor
    }
}
