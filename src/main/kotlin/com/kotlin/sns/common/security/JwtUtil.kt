package com.kotlin.sns.common.security

import com.kotlin.sns.common.exception.CustomException
import com.kotlin.sns.common.exception.ErrorCode
import com.kotlin.sns.domain.Member.entity.Member
import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import jakarta.annotation.PostConstruct
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component
import java.util.*

/**
 * jwt 토큰 생성, 유효성 검증 관련 로직 작성
 *
 */
@Component
class JwtUtil(
    private val userServiceDetails: UserDetailsService,
    @Value("\${jwt.secret}") private val secret: String
)
{

    @Value("\${jwt.expiration}")
    private var jwtExpiration: Long = 0

    @Value("\${jwt.refreshExpiration}")
    private var refreshExpiration : Long = 0

    private val logger = KotlinLogging.logger{}

    /**
     * 애플리케이션 시작 시 한 번만 실행해서 초기화하는 메서드
     */
    @PostConstruct
    fun jwtInit() {
        val encodedSecret = Base64.getEncoder().encodeToString(secret.toByteArray())
        logger.debug { "secret code value : $encodedSecret" }
        logger.debug { "jwt expire time : $jwtExpiration" }
    }

    fun getAuthentication(username : String) : Authentication{
        val userDetails = userServiceDetails.loadUserByUsername(username)
        return UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
    }

    fun createToken(username : String, roles : List<String>) : String{
        val claim = Jwts.claims().setSubject(username)
        claim["roles"] = roles
        val now = Date()

        return "Bearer " + Jwts.builder()
            .setClaims(claim)
            .setIssuedAt(now)
            .setExpiration(Date(now.time + jwtExpiration))
            .signWith(Keys.hmacShaKeyFor(secret.toByteArray()), SignatureAlgorithm.HS256)
            .compact()
    }

    fun createRefreshToken(username : String) : String{
        val claim = Jwts.claims().setSubject(username)
        val now = Date()

        return "Bearer " + Jwts.builder()
            .setClaims(claim)
            .setIssuedAt(now)
            .setExpiration(Date(now.time + refreshExpiration))
            .signWith(Keys.hmacShaKeyFor(secret.toByteArray()), SignatureAlgorithm.HS256)
            .compact()
    }

    fun validateToken(token : String) : Boolean {

        logger.debug { "token value : $token" }
        val claim = Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(secret.toByteArray()))
            .build()
            .parseClaimsJws(token)
            .body

        return !claim.expiration.before(Date())
    }

    fun resolveToken(request: HttpServletRequest) : String?{
        val header = request.getHeader("auth_token")
        return if (header != null && header.startsWith("Bearer ")) {
            header.substringAfter("Bearer ")
        } else {
            null
        }
    }

    fun resolveUsername(token : String) : String{
        logger.debug{ "token value : $token "}

        if(token.startsWith("Bearer ")) {
            token.substringAfter("Bearer ")
        }

        return Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(secret.toByteArray()))
            .build()
            .parseClaimsJws(token)
            .body
            .subject
    }

    /**
     * 포스팅 수정, 삭제 권한을 가진 사용자인지 체크
     *
     * @param posting
     */
    fun checkPermission(makerId : Long) {
        /**
         * principal -> 인증된 사용자의 주요 정보
         * 주로 UserDetails를 구현한 객체. (member)
         * spring security는 securityContext를 thread local에 저장한다
         */
        val currentUser = SecurityContextHolder.getContext().authentication.principal as Member
        logger.debug { "user id : ${currentUser.id}" }
        if (makerId != currentUser.id) {
            logger.debug { "jwtUtil checkPermission : cannot modify or delete" }
            throw CustomException(ErrorCode.CANNOT_MODIFY_OR_DELETE_POSTING)
        }
    }
}