package com.kotlin.sns.domain.Member.service

import com.kotlin.sns.domain.Member.dto.request.RequestCreateMemberDto
import com.kotlin.sns.domain.Member.dto.request.RequestUpdateMemberDto
import com.kotlin.sns.domain.Member.dto.response.ResponseMemberDto
import com.kotlin.sns.domain.Member.entity.Member


interface MemberService {
    fun findMemberById(memberId : Long):ResponseMemberDto
    fun findMemberByEmail(email : String):ResponseMemberDto
    fun createMember(requestCreateMemberDto: RequestCreateMemberDto) : ResponseMemberDto
    fun updateMember(requestUpdateMemberDto: RequestUpdateMemberDto) : ResponseMemberDto
    fun updateProfileImage(memberId: Long, imageUrl : String)
    fun deleteMember(memberId: Long)
}

