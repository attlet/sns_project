package com.kotlin.sns.domain.Member.dto.request

/**
 * member 생성 시 양식 dto
 *
 * @property name
 * @property email
 * @property password
 * @property profileImageUrl
 */
data class RequestCreateMemberDto(
    val userId : String,
    val name : String,
    val email : String,
    val pw : String
)
{

}