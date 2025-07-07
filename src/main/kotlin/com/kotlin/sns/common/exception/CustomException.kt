package com.kotlin.sns.common.exception

import org.springframework.http.HttpStatus
import java.lang.RuntimeException

open class CustomException(
    val errorCode: ErrorCode
) : RuntimeException(errorCode.message){
    // 인자가 없는 경우를 위한 편의 생성자
}