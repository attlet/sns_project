package com.kotlin.sns.domain.Authentication.service.Impl

import com.kotlin.sns.common.security.JwtUtil
import com.kotlin.sns.domain.Authentication.dto.request.RequestSignInDto
import com.kotlin.sns.domain.Authentication.dto.request.RequestSignUpDto
import com.kotlin.sns.domain.Authentication.dto.response.ResponseSignInDto
import com.kotlin.sns.domain.Authentication.service.AuthenticationService
import com.kotlin.sns.domain.Member.dto.response.ResponseMemberDto
import com.kotlin.sns.domain.Member.entity.Member
import com.kotlin.sns.domain.Member.mapper.MemberMapper
import com.kotlin.sns.domain.Member.repository.MemberRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

/**
 * 인증/인가 관련 로직 처리
 *
 * @property memberRepository
 */
@Service
class AuthenticationServiceImpl(
    private val memberRepository: MemberRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil,
    private val memberMapper: MemberMapper
) : AuthenticationService {

    /**
     * 회원 가입 메서드
     * 아이디, 이메일 중복 검사
     * 권한은 컨트롤러에서 구분해서 넘겨주도록 함
     *
     * @param requestSignUpDto
     * @return
     */
    override fun signUp(requestSignUpDto: RequestSignUpDto): ResponseMemberDto {
        val id = requestSignUpDto.id
        val name = requestSignUpDto.name
        val password = requestSignUpDto.password
        val email = requestSignUpDto.email
        val roles = requestSignUpDto.roles

        if(memberRepository.findByUserId(id).isPresent){
            throw RuntimeException("dup id")
        }

        if(memberRepository.findByEmail(email).isPresent){
            throw RuntimeException("dup email")
        }

        val member = Member(
            userId = id,
            name = name,
            pw = passwordEncoder.encode(password),
            email = email,
            roles = listOf(roles)
        )

        val savedMember = memberRepository.save(member)

        return memberMapper.toDto(savedMember)
    }

    override fun signIn(requestSignInDto: RequestSignInDto) : ResponseSignInDto{
        val id = requestSignInDto.id
        val password = requestSignInDto.password

        val member = memberRepository.findByUserId(id)
            .orElseThrow { IllegalArgumentException("invalid user id : $id") }

        if(!passwordEncoder.matches(password, member.pw)){
            throw IllegalArgumentException("invalid password")
        }

        val token = jwtUtil.createToken(id, member.roles)

        return ResponseSignInDto(token = token)
    }

    override fun logout() {
        TODO("Not yet implemented")
    }
}