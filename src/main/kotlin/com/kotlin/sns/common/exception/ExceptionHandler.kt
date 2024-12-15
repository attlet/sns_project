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
            errorType = e.exception.exceptionDesc,
            errorCode = e.status.value().toString(),
            status = e.status,
            message = e.message
        )

       return ResponseEntity(exceptionResponse, e.status)
    }

//    @ExceptionHandler(Exception::class)
//    fun handleGenericException(e: Exception): ResponseEntity<ExceptionResponse> {
//
//        val exceptionResponse = ExceptionResponse(
//            errorType = "INTERVAL ERROR",
//            errorCode = "500",
//            status = null,
//            message = e.stackTraceToString()
//        )
//        return ResponseEntity(exceptionResponse, HttpStatus.INTERNAL_SERVER_ERROR)
//    }
}

data class ExceptionResponse(
    val errorType : String,
    val errorCode : String,
    val status : HttpStatus?,
    val message : String?
)

