package com.kotlin.sns.common.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import jakarta.annotation.PostConstruct
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

/**
 * jwt 토큰 생성, 유효성 검증 관련 로직 작성
 *
 */
@Component
class JwtUtil {

    @Value("\${jwt.expiration}")
    private var jwtExpiration: Long = 0

    @Value("\${jwt.secret}")
    private lateinit var secret: String

    /**
     * 애플리케이션 시작 시 한 번만 실행해서 초기화하는 메서드
     */
    @PostConstruct
    fun jwtInit() {
        secret = Base64.getEncoder().encodeToString(secret.toByteArray())
    }

    fun createToken(username : String, roles : List<String>) : String{
        val claim = Jwts.claims().setSubject(username)
        claim.put("roles", roles)
        val now = Date()

        return "Bearer " + Jwts.builder()
            .setClaims(claim)
            .setIssuedAt(now)
            .setExpiration(Date(now.time + jwtExpiration))
            .signWith(Keys.hmacShaKeyFor(secret.toByteArray()), SignatureAlgorithm.HS256)
            .compact()
    }

    fun validateToken(token : String) : Boolean {
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
        val subject = Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(secret.toByteArray()))
            .build()
            .parseClaimsJws(token)
            .body
            .subject

        return subject
    }
}