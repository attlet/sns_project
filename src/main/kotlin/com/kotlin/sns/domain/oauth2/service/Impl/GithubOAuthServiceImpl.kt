package com.kotlin.sns.domain.oauth2.service.Impl

import com.kotlin.sns.domain.Authentication.dto.GithubTokenResponse
import com.kotlin.sns.domain.Authentication.dto.JwtResponse
import com.kotlin.sns.domain.Authentication.dto.request.RequestOAuthToken
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class GithubOAuthServiceImpl (
    private val restTemplate: RestTemplate,
    @Value("\${spring.security.oauth2.client.registration.github.client-id}")
    private val clientId: String,
    @Value("\${spring.security.oauth2.client.registration.github.client-secret}")
    private val clientSecret: String
) {
    private val tokenUrl = "https://github.com/login/oauth/access_token"

    fun githubLogin(code: String): JwtResponse {
        val accessToken = getAccessToken(code)
        // TODO: accessToken을 사용하여 GitHub에서 사용자 정보를 가져오는 로직 추가
        // TODO: 사용자 정보를 기반으로 우리 서비스의 DB에 사용자를 저장하거나 업데이트하는 로직 추가
        // TODO: 우리 서비스의 JWT를 생성하여 반환하는 로직 추가

        // 임시로 GitHub AccessToken을 반환
        return JwtResponse(accessToken = accessToken)
    }

    private fun getAccessToken(code: String): String {
        val headers = HttpHeaders().apply {
            accept = listOf(MediaType.APPLICATION_JSON)
        }

        val requestPayload = RequestOAuthToken(
            clientId = clientId,
            clientSecret = clientSecret,
            code = code
        )

        val requestEntity = HttpEntity(requestPayload, headers)

        val responseEntity = restTemplate.exchange(
            tokenUrl,
            HttpMethod.POST,
            requestEntity,
            GithubTokenResponse::class.java
        )

        return responseEntity.body?.accessToken ?: throw IllegalStateException("Access Token을 가져오는데 실패했습니다.")
    }
}