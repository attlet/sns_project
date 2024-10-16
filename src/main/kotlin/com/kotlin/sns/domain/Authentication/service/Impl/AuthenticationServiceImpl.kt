package com.kotlin.sns.domain.Authentication.service.Impl

import com.kotlin.sns.domain.Authentication.dto.request.RequestSignInDto
import com.kotlin.sns.domain.Authentication.dto.request.RequestSignUpDto
import com.kotlin.sns.domain.Authentication.service.AuthenticationService
import com.kotlin.sns.domain.Member.dto.response.ResponseMemberDto
import com.kotlin.sns.domain.Member.entity.Member
import com.kotlin.sns.domain.Member.mapper.MemberMapper
import com.kotlin.sns.domain.Member.repository.MemberRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.lang.RuntimeException

/**
 * 인증/인가 관련 로직 처리
 *
 * @property memberRepository
 */
@Service
class AuthenticationServiceImpl(
    private val memberRepository: MemberRepository,
    private val passwordEncoder: PasswordEncoder,
    private val memberMapper: MemberMapper
) : AuthenticationService {

    /**
     * 회원 가입 메서드
     * 아이디, 이메일 중복 검사
     *
     * @param requestSignUpDto
     * @return
     */
    override fun signUp(requestSignUpDto: RequestSignUpDto): ResponseMemberDto {
        val id = requestSignUpDto.id
        val name = requestSignUpDto.name
        val password = requestSignUpDto.password
        val email = requestSignUpDto.email

        if(memberRepository.findByUserId(id).isPresent){
            throw RuntimeException("dup id")
        }

        if(memberRepository.findByEmail(email).isPresent){
            throw RuntimeException("dup email")
        }

        val member = Member(
            userId = id,
            name = name,
            password = passwordEncoder.encode(password),
            email = email
        )

        val savedMember = memberRepository.save(member)

        return memberMapper.toDto(savedMember)
    }

    override fun signIn(requestSignInDto: RequestSignInDto) {
        TODO("Not yet implemented")
    }

    override fun logout() {
        TODO("Not yet implemented")
    }
}