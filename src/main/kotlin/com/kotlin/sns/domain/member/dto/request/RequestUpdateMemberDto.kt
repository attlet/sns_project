package com.kotlin.sns.domain.member.dto.request

/**
 * member 정보 수정 시 양식 dto
 *
 * @property name
 * @property password
 * @property profileImageUrl
 */
data class RequestUpdateMemberDto(
    val memberId : Long,
    val name : String? = null,
    val email : String? = null,
    val pw : String? = null
) {

}