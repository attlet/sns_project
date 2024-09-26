package com.kotlin.sns.domain.Member.dto.response


data class ResponseFindMemberDto(
    val name : String,
    val email : String,
    val profileImageUrl : String? = null
) {
}