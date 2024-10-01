package com.kotlin.sns.domain.Member.dto.request

/**
 * member 정보 수정 시 양식 dto
 *
 * @property name
 * @property password
 * @property profileImageUrl
 */
data class RequestUpdateMemberDto(
    val memberId : Long,
    val name : String,
    val email : String,           //mail기반으로 member 조회해야해서 필수로 입력 필요
    val password : String,
    val profileImageUrl : String
) {

}