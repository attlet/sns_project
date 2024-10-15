package com.kotlin.sns.domain.Authentication.controller

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
@Tag(name = "authenticationController", description = "인증/인가 관련 api")
class AuthenticationController {

}