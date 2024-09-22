package com.kotlin.sns.domain.Member.service.Impl

import com.kotlin.sns.domain.Member.dto.request.RequestCreateMemberDto
import com.kotlin.sns.domain.Member.entity.Member
import com.kotlin.sns.domain.Member.repository.MemberRepository
import com.kotlin.sns.domain.Member.service.MemberService
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException

@Service
class MemberServiceImpl(private val memberRepository: MemberRepository) : MemberService{
    override fun findMemberById(memberId: Long): Member {
        return memberRepository.findById(memberId)
            .orElseThrow{IllegalArgumentException("invalid member id : $memberId")}
    }

    override fun findMemberByEmail(email: String): Member {
        return memberRepository.findByEmail(email)
    }

    override fun createMember(requestCreateMemberDto: RequestCreateMemberDto) {

    }

    override fun updateMember() {
        TODO("Not yet implemented")
    }

    override fun deleteMember() {
        TODO("Not yet implemented")
    }
}