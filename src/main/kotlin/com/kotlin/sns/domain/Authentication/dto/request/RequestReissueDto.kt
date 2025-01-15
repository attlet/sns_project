package com.kotlin.sns.domain.Authentication.dto.request

data class RequestReissueDto(
    val id : String,
    val refreshToken : String
) {
}