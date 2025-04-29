package com.kotlin.sns.common.exception

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.ErrorResponse
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

@RestControllerAdvice
class ExceptionHandler {
    @ExceptionHandler(CustomException::class)
    fun handleCustomException(e : CustomException, request : HttpServletRequest) : ResponseEntity<ExceptionResponse>{
        val exceptionResponse = ExceptionResponse(
            errorCode = e.errorCode.code,
            status = e.errorCode.status,
            message = e.errorCode.message
        )

       return ResponseEntity(exceptionResponse, e.errorCode.status)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(e: Exception): ResponseEntity<ExceptionResponse> {

        val exceptionResponse = ExceptionResponse(
            errorCode = "500",
            status = null,
            message = e.message
        )
        return ResponseEntity(exceptionResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}

data class ExceptionResponse(
    val errorCode : String,
    val status : HttpStatus?,
    val message : String?
)

