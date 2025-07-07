package com.kotlin.sns.domain.Authentication.service.Impl

import com.kotlin.sns.common.exception.CustomException
import com.kotlin.sns.common.exception.ErrorCode
import com.kotlin.sns.common.security.JwtUtil
import com.kotlin.sns.domain.Authentication.dto.request.RequestReissueDto
import com.kotlin.sns.domain.Authentication.dto.request.RequestSignInDto
import com.kotlin.sns.domain.Authentication.dto.request.RequestSignUpDto
import com.kotlin.sns.domain.Authentication.dto.response.ResponseReissueDto
import com.kotlin.sns.domain.Authentication.dto.response.ResponseSignInDto
import com.kotlin.sns.domain.Authentication.service.AuthenticationService
import com.kotlin.sns.domain.Member.entity.Member
import com.kotlin.sns.domain.Member.repository.MemberRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.TimeUnit

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
    private val redisTemplate: RedisTemplate<String, String>,
    @Value("\${jwt.expiration}") private val jwtExpiration: Long = 0,
    @Value("\${jwt.refreshExpiration}") private val refreshExpiration : Long = 0
) : AuthenticationService {

    private val logger = KotlinLogging.logger {}


    /**
     * 회원 가입 메서드
     * 아이디, 이메일 중복 검사
     * 권한은 컨트롤러에서 구분해서 넘겨주도록 함
     *
     * @param requestSignUpDto
     * @return
     */
    @Transactional
    override fun signUp(requestSignUpDto: RequestSignUpDto): String {
        logger.info { "AuthenticationService signUp " }
        val id = requestSignUpDto.id
        val name = requestSignUpDto.name
        val password = requestSignUpDto.password
        val email = requestSignUpDto.email
        val roles = requestSignUpDto.roles

        // 아이디 중복 체크
        if (memberRepository.findByUserId(id).isPresent) {
            throw CustomException(ErrorCode.USERID_DUPLICATION)
        }

        // 이메일 중복 체크
        if (memberRepository.findByEmail(email).isPresent) {
            throw CustomException(ErrorCode.EMAIL_DUPLICATION)
        }

        logger.debug { "user id : $id , email : $email" }

        val member = Member(
            userId = id,
            name = name,
            pw = passwordEncoder.encode(password),
            email = email,
            roles = listOf(roles)
        )

        val savedMember = memberRepository.save(member)

        return "success"
    }

    /**
     * 로그인 처리 메서드
     *
     * @param requestSignInDto
     * @return
     */
    @Transactional(readOnly = true)
    override fun signIn(requestSignInDto: RequestSignInDto): ResponseSignInDto {
        logger.info { "Authentication signIn" }

        val id = requestSignInDto.id
        val password = requestSignInDto.password

        //1. id에 대응되는 member get
        val member = memberRepository.findByUserId(id)
            .orElseThrow {
                CustomException(ErrorCode.MEMBER_NOT_FOUND)
            }

        //2. 비밀번호 일치하는지 확인
        if (!passwordEncoder.matches(password, member.pw)) {
            throw CustomException(ErrorCode.INVALID_PASSWORD)
        }

        logger.debug { "user id : ${member.id} , password : $password" }

        //3. access token , refresh token 생성
        val token = jwtUtil.createToken(id, member.roles)
        val refreshToken = jwtUtil.createRefreshToken(id)

        logger.debug { "token : $token , refresh token : $refreshToken" }


        //4. refresh token은 redis에도 세팅. key : user id,  value : refresh token
        redisTemplate.opsForValue().set(id, refreshToken, refreshExpiration, TimeUnit.MILLISECONDS)


        return ResponseSignInDto(token = token, refreshToken = refreshToken)
    }

    /**
     * access token이 만료되었을 때, refresh token을 통해 새로운 token을 생성하는 메서드
     *
     * @param refreshToken
     * @return
     */
    @Transactional
    override fun reissue(requestReissueDto: RequestReissueDto): ResponseReissueDto {

        logger.info{ "Authentication reissue"}
        val refreshToken = requestReissueDto.refreshToken
        val id = requestReissueDto.id

        //1. refresh token 으로부터 user id 추출
        val userId = jwtUtil.resolveUsername(refreshToken)

        //2. user id 를 key 로 redis에 저장된 refresh token을 확인
        val storedRefreshToken = redisTemplate.opsForValue().get(userId)

        //3. redis에 저장된 refresh token과 , 요청으로 들어온 refresh token을 비교. 다르면 exception 발생
        if (refreshToken != storedRefreshToken) {
            throw CustomException(ErrorCode.DIFFERENT_REFRESH_TOKEN)
        }

        //4. 같으면 새로운 access token 생성해서 반환
        val member = memberRepository.findByUserId(userId)
            .orElseThrow {
                CustomException(ErrorCode.MEMBER_NOT_FOUND)
            }

        //5. redis에 해당 사용자의 refresh token을 새로 갱신
        val newRefreshToken = jwtUtil.createRefreshToken(userId)
        redisTemplate.opsForValue().set(id, newRefreshToken)
        redisTemplate.expire(id, refreshExpiration, TimeUnit.MILLISECONDS)

        //6. 새로운 access token 생성
        val newToken = jwtUtil.createToken(userId, member.roles)

        return ResponseReissueDto(id = id, accessToken = newToken, refreshToken = newRefreshToken)
    }

    override fun logout() {

    }
}
