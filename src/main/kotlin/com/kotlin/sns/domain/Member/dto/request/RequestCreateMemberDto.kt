package com.kotlin.sns.domain.Member.dto.request

/*
member 생성 때 사용하는 dto
 */
data class RequestCreateMemberDto(
    val name : String,
    val email : String,
    val password : String? = null,
    val profileImageUrl : String? = null
)
{

}