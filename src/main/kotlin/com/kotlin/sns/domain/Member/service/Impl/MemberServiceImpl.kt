package com.kotlin.sns.domain.Member.service.Impl

import com.kotlin.sns.domain.Member.dto.request.RequestCreateMemberDto
import com.kotlin.sns.domain.Member.dto.request.RequestUpdateMemberDto
import com.kotlin.sns.domain.Member.dto.response.ResponseFindMemberDto
import com.kotlin.sns.domain.Member.dto.response.ResponseMemberDto
import com.kotlin.sns.domain.Member.entity.Member
import com.kotlin.sns.domain.Member.mapper.MemberMapper
import com.kotlin.sns.domain.Member.repository.MemberRepository
import com.kotlin.sns.domain.Member.service.MemberService
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException

/**
 * MemberService
 * member의 비즈니스 로직 처리
 *
 * @property memberRepository
 * @property memberMapper
 */
@Service
class MemberServiceImpl(
    private val memberRepository: MemberRepository,
    private val memberMapper: MemberMapper) : MemberService{

    /**
     * uuid 기반으로 member 반환
     *
     * @param memberId
     * @return
     */
    override fun findMemberById(memberId: Long): Member {
        return memberRepository.findById(memberId)
            .orElseThrow{IllegalArgumentException("invalid member id : $memberId")}
    }

    /**
     * email 기반으로 member 반환
     *
     * @param email
     * @return
     */
    override fun findMemberByEmail(email: String): Member {
        return memberRepository.findByEmail(email)
            .orElseThrow{IllegalArgumentException("invalid member email : $email")}
    }

    /**
     * TODO
     *
     * @param requestCreateMemberDto
     * @return
     */
    override fun createMember(requestCreateMemberDto: RequestCreateMemberDto) : ResponseFindMemberDto{
        val savedMember = memberMapper.toEntity(requestCreateMemberDto)
        val member = memberRepository.save(savedMember)
        return memberMapper.toDto(member);
    }

    override fun updateMember(requestUpdateMemberDto: RequestUpdateMemberDto): ResponseMemberDto {
        val memberId = requestUpdateMemberDto.memberId
        val member = memberRepository.findById(memberId)
            .orElseThrow {IllegalArgumentException("invalid member id : $memberId") }

        return memberMapper.toDto(member)

    }

    override fun deleteMember(memberId: Long) {
        TODO("Not yet implemented")
    }
}