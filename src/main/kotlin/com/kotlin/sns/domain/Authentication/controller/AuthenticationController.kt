package com.kotlin.sns.domain.Authentication.controller

import com.kotlin.sns.domain.Authentication.dto.request.RequestSignInDto
import com.kotlin.sns.domain.Authentication.dto.request.RequestSignUpDto
import com.kotlin.sns.domain.Authentication.dto.response.ResponseSignInDto
import com.kotlin.sns.domain.Authentication.dto.response.ResponseSignUpDto
import com.kotlin.sns.domain.Authentication.service.AuthenticationService
import com.kotlin.sns.domain.Member.dto.response.ResponseMemberDto
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
@Tag(name = "authenticationController", description = "인증/인가 관련 api")
class AuthenticationController (
    private val authenticationService: AuthenticationService
){

    @PostMapping("/signUp")
    fun signUp(@RequestBody requestSignUpDto: RequestSignUpDto) : ResponseMemberDto{
        return authenticationService.signUp(requestSignUpDto)
    }

    @PostMapping("/signIn")
    fun signIn(@RequestBody requestSignInDto: RequestSignInDto) : ResponseSignInDto {
        return authenticationService.signIn(requestSignInDto)
    }

}