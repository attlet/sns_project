package com.kotlin.sns.common.exception

import org.springframework.http.HttpStatus
import java.lang.RuntimeException

class CustomException(
    val exception: ExceptionConst,
    val status : HttpStatus,

) : RuntimeException("${exception.exceptionDesc}"){

}