package com.kotlin.sns.domain.member.dto.response

/**
 * 기본적인 member 처리 반환 dto
 *
 * @property name
 * @property email
 * @property profileImageUrl
 */
data class ResponseMemberDto(
    val name : String,
    val email : String,
){
}