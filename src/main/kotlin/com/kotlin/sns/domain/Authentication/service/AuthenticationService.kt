package com.kotlin.sns.domain.Authentication.service

import com.kotlin.sns.domain.Authentication.dto.request.RequestSignInDto
import com.kotlin.sns.domain.Authentication.dto.request.RequestSignUpDto
import com.kotlin.sns.domain.Member.dto.response.ResponseMemberDto

interface AuthenticationService {
    fun signUp(requestSignUpDto: RequestSignUpDto) : ResponseMemberDto;
    fun signIn(requestSignInDto: RequestSignInDto);
    fun logout();
}