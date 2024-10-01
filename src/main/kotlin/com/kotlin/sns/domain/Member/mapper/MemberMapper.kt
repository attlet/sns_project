package com.kotlin.sns.domain.Member.mapper

import com.kotlin.sns.domain.Member.dto.request.RequestCreateMemberDto
import com.kotlin.sns.domain.Member.dto.request.RequestUpdateMemberDto
import com.kotlin.sns.domain.Member.dto.response.ResponseFindMemberDto
import com.kotlin.sns.domain.Member.dto.response.ResponseMemberDto
import com.kotlin.sns.domain.Member.entity.Member
import org.mapstruct.Mapper

/**
 * member 에 대한 entity <-> dto 변환 mapper
 *
 */
@Mapper(componentModel = "spring")  //spring bean으로 mapper 인터페이스를 등록할 수 있음
interface MemberMapper {

    fun toEntity(dto : RequestCreateMemberDto) : Member
    fun toEntity(dto : RequestUpdateMemberDto) : Member
    fun toDto(entity : Member) : ResponseMemberDto
}