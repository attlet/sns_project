package com.kotlin.sns.domain.Authentication.service.Impl

import com.kotlin.sns.common.exception.CustomException
import com.kotlin.sns.common.exception.ExceptionConst
import com.kotlin.sns.common.security.JwtUtil
import com.kotlin.sns.domain.Authentication.dto.request.RequestSignInDto
import com.kotlin.sns.domain.Authentication.dto.request.RequestSignUpDto
import com.kotlin.sns.domain.Authentication.dto.response.ResponseSignInDto
import com.kotlin.sns.domain.Authentication.service.AuthenticationService
import com.kotlin.sns.domain.Member.dto.response.ResponseMemberDto
import com.kotlin.sns.domain.Member.entity.Member
import com.kotlin.sns.domain.Member.mapper.MemberMapper
import com.kotlin.sns.domain.Member.repository.MemberRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

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
    private val memberMapper: MemberMapper,
) : AuthenticationService {

    private val logging = KotlinLogging.logger {}

    /**
     * 회원 가입 메서드
     * 아이디, 이메일 중복 검사
     * 권한은 컨트롤러에서 구분해서 넘겨주도록 함
     *
     * @param requestSignUpDto
     * @return
     */
    @Transactional
    override fun signUp(requestSignUpDto: RequestSignUpDto): ResponseMemberDto {
        logging.info { "AuthenticationService signUp " }
        val id = requestSignUpDto.id
        val name = requestSignUpDto.name
        val password = requestSignUpDto.password
        val email = requestSignUpDto.email
        val roles = requestSignUpDto.roles

        // 아이디 중복 체크
        if (memberRepository.findByUserId(id).isPresent) {
            throw CustomException(
                ExceptionConst.MEMBER,
                HttpStatus.CONFLICT,
                "User ID $id already exists"
            )
        }

        // 이메일 중복 체크
        if (memberRepository.findByEmail(email).isPresent) {
            throw CustomException(
                ExceptionConst.MEMBER,
                HttpStatus.CONFLICT,
                "Email $email already exists"
            )
        }

        logging.debug { "user id : $id , email : $email" }

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

    /**
     * 로그인 처리 메서드
     *
     * @param requestSignInDto
     * @return
     */
    @Transactional(readOnly = true)
    override fun signIn(requestSignInDto: RequestSignInDto): ResponseSignInDto {
        logging.info { "Authentication signIn" }

        val id = requestSignInDto.id
        val password = requestSignInDto.password

        val member = memberRepository.findByUserId(id)
            .orElseThrow {
                CustomException(
                    ExceptionConst.MEMBER,
                    HttpStatus.NOT_FOUND,
                    "User ID $id not found"
                )
            }

        // 비밀번호 확인
        if (!passwordEncoder.matches(password, member.pw)) {
            throw CustomException(
                ExceptionConst.AUTH,
                HttpStatus.UNAUTHORIZED,
                "Invalid password"
            )
        }

        logging.debug { "user id : $id , password : $password" }
        val token = jwtUtil.createToken(id, member.roles)

        return ResponseSignInDto(token = token)
    }

    override fun logout() {

    }
}
