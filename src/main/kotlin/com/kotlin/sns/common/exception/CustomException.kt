package com.kotlin.sns.common.exception

import org.springframework.http.HttpStatus
import java.lang.RuntimeException

open class CustomException(
    val errorCode: ErrorCode,
    val args: Array<Any>? = null // 메시지 포맷팅을 위한 인자 (예: "ID {0} not found" 에서 {0}에 들어갈 값)
) : RuntimeException(errorCode.message){
    // 인자가 없는 경우를 위한 편의 생성자
    constructor(errorCode: ErrorCode) : this(errorCode, null)
}