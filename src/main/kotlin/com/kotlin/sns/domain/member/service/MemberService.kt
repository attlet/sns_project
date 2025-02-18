package com.kotlin.sns.domain.member.service

import com.kotlin.sns.domain.member.dto.request.RequestCreateMemberDto
import com.kotlin.sns.domain.member.dto.request.RequestUpdateMemberDto
import com.kotlin.sns.domain.member.dto.response.ResponseMemberDto


interface MemberService {
    fun findMemberById(memberId : Long):ResponseMemberDto
    fun findMemberByEmail(email : String):ResponseMemberDto
    fun createMember(requestCreateMemberDto: RequestCreateMemberDto) : ResponseMemberDto
    fun updateMember(requestUpdateMemberDto: RequestUpdateMemberDto) : ResponseMemberDto
    fun deleteMember(memberId: Long)
}

