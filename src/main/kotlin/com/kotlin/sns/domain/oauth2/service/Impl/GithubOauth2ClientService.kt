package com.kotlin.sns.domain.oauth2.service.Impl

import com.kotlin.sns.domain.oauth2.service.OAuth2ClientService
import com.kotlin.sns.domain.Member.entity.Member
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.stereotype.Service

@Service
class GithubOauth2ClientService() : OAuth2ClientService {
    override fun supports(registrationId: String): Boolean {
        return "github".equals(registrationId, ignoreCase = true)
    }

    override fun processUser(userRequest: OAuth2UserRequest): Member {
        // TODO: Implement logic to process GitHub user info and return a Member entity
        throw NotImplementedError("GitHub user processing is not yet implemented.")
    }
}