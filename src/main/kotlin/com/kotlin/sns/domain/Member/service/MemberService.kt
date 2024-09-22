package com.kotlin.sns.domain.Member.service

import com.kotlin.sns.domain.Member.dto.request.RequestCreateMemberDto
import com.kotlin.sns.domain.Member.entity.Member
import com.kotlin.sns.domain.Member.repository.MemberRepository
import org.springframework.stereotype.Service


interface MemberService {
    fun findMemberById(memberId : Long):Member
    fun findMemberByEmail(email : String):Member
    fun createMember(requestCreateMemberDto: RequestCreateMemberDto)
    fun updateMember()
    fun deleteMember()
}

