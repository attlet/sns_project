package com.kotlin.sns.common.exception

import org.springframework.http.HttpStatus
import java.lang.RuntimeException

open class CustomException(
    val errorCode: ErrorCode,
    cause: Throwable? = null
) : RuntimeException(errorCode.message, cause)