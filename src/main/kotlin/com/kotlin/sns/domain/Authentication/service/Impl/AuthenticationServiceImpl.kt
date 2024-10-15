package com.kotlin.sns.domain.Authentication.service.Impl

import com.kotlin.sns.domain.Authentication.service.AuthenticationService
import com.kotlin.sns.domain.Member.repository.MemberRepository
import org.springframework.stereotype.Service

/**
 * 인증/인가 관련 로직 처리
 *
 * @property memberRepository
 */
@Service
class AuthenticationServiceImpl(
    private val memberRepository: MemberRepository
) : AuthenticationService {
    override fun signIn() {
        TODO("Not yet implemented")
    }

    override fun signOut() {
        TODO("Not yet implemented")
    }
}