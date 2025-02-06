package com.kotlin.sns.common.aop

import io.github.oshai.kotlinlogging.KotlinLogging
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.stereotype.Component

@Aspect
@Component
class ExecutionTimeAspect {
    private val logger = KotlinLogging.logger{}

    /**
     * service 어노테이션이 붙은 모든 클래스의 메서드에 대해 실행됨.
     *
     * ProceedingJoinPoint는 around 어드바이스 에서 사용되는 join point의 확장 인터페이스
     * 실제 target 메서드를 실행할 수 있는 기능을 제공한다.
     *
     * @param joinPoint
     * @return
     */
    @Around("within(@org.springframework.stereotype.Service *)")
    fun logExecutionTime(joinPoint : ProceedingJoinPoint) : Any? {
        val startTime = System.currentTimeMillis()

        val result = joinPoint.proceed()

        val duration = System.currentTimeMillis() - startTime
        logger.info{ "[Execution Time] ${joinPoint.signature} 실행 시간 : $duration ms"}
        return result
    }
}