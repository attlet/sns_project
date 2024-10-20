package com.kotlin.sns.common.exception

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.ErrorResponse
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionHandler {
    @ExceptionHandler(CustomException::class)
    fun handleCustomException(e : CustomException, request : HttpServletRequest) : ResponseEntity<ExceptionResponse>{
        val exceptionResponse = ExceptionResponse(
            errorType = e.exception.exceptionDesc,
            status = e.status,
            message = e.message
        )

       return ResponseEntity(exceptionResponse, e.status)
    }
}

data class ExceptionResponse(
    val errorType : String,
    val status : HttpStatus,
    val message : String?
)

