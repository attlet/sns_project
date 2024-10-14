package com.kotlin.sns.common.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * 사용자 request 요청 선 처리하는 filter
 * 토큰 검증 진행
 *
 * @property jwtUtil
 * @property userServiceDetails
 */
@Component
class JwtAuthenticationFilter(
    private val jwtUtil: JwtUtil,
    private val userServiceDetails: UserDetailsService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = jwtUtil.resolveToken(request)

        if(token != null && jwtUtil.validateToken(token)){
            val username = jwtUtil.resolveUsername(token)
            val userDetails = userServiceDetails.loadUserByUsername(username)

            val authToken = UsernamePasswordAuthenticationToken(userDetails, "", userDetails.authorities)
            SecurityContextHolder.getContext().authentication = authToken
        }

        filterChain.doFilter(request, response)
    }

}