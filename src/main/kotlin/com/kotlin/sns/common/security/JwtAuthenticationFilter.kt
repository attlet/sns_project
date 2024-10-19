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

    /**
     * 사용자 request 가로채는 부분
     *
     * @param request
     * @param response
     * @param filterChain
     */
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = jwtUtil.resolveToken(request)

        //사용자의 토큰이 유효한지 체크
        if(token != null && jwtUtil.validateToken(token)){
            val username = jwtUtil.resolveUsername(token) //토큰으로부터 사용자 username 추출
            val userDetails = userServiceDetails.loadUserByUsername(username)  //토큰 등록을 위한 userDetail객체 반환

            val authToken = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
            SecurityContextHolder.getContext().authentication = authToken //security context에 사용자 등록, 접속 허용
        }

        filterChain.doFilter(request, response)
    }

}