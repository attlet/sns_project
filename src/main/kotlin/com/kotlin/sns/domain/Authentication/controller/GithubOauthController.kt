package com.kotlin.sns.domain.Authentication.controller

import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.io.IOException

@RestController
@RequestMapping("/auth")
class OAuthController(
    @Value("\${spring.security.oauth2.client.registration.github.client-id}")
    private val clientId: String,
    @Value("\${spring.security.oauth2.client.registration.github.redirect-uri}")
    private val redirectUri: String
) {

    /**
     * GitHub OAuth 로그인을 시작하기 위해 사용자를 GitHub 인증 페이지로 리디렉션합니다.
     */
    @GetMapping("/github/login")
    @Throws(IOException::class)
    fun redirectToGithub(response: HttpServletResponse) {
        val scope = "read:user,user:email"
        val githubAuthUrl = "https://github.com/login/oauth/authorize?client_id=$clientId&redirect_uri=$redirectUri&scope=$scope"
        response.sendRedirect(githubAuthUrl)
    }

    /**
     * GitHub에서 인증 후 리디렉션되는 콜백을 처리합니다.
     * 'code'를 받아 액세스 토큰을 요청하고, 사용자 정보를 가져와 JWT를 생성합니다.
     */
    @GetMapping("/callback")
    fun githubCallback(@RequestParam("code") code: String): ResponseEntity<*> {
        val jwtResponse = oAuthService.githubLogin(code)
        return ResponseEntity.ok(jwtResponse)
    }
}
