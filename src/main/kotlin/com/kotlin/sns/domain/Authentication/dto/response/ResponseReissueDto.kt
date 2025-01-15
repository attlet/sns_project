package com.kotlin.sns.domain.Authentication.dto.response

data class ResponseReissueDto(
    val id : String,
    val accessToken : String,
    val refreshToken : String
) {
}