package com.kotlin.sns.domain.Member.dto.response

/**
 * 멤버 조회 시 반환 dto
 *
 * @property name
 * @property email
 * @property profileImageUrl
 */
data class ResponseFindMemberDto(
    val name : String,
    val email : String,
    val profileImageUrl : String? = null
) {
}