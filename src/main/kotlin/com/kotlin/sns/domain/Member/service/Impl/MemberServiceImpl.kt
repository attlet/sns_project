package com.kotlin.sns.domain.Member.service.Impl

import com.kotlin.sns.domain.Member.dto.request.RequestCreateMemberDto
import com.kotlin.sns.domain.Member.dto.request.RequestUpdateMemberDto
import com.kotlin.sns.domain.Member.dto.response.ResponseMemberDto
import com.kotlin.sns.domain.Member.mapper.MemberMapper
import com.kotlin.sns.domain.Member.repository.MemberRepository
import com.kotlin.sns.domain.Member.service.MemberService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import kotlin.reflect.full.memberProperties

/**
 * member의 비즈니스 로직 처리
 *
 * @property memberRepository
 * @property memberMapper
 */
@Service
class MemberServiceImpl(
    private val memberRepository: MemberRepository,
    private val memberMapper: MemberMapper) : MemberService, UserDetailsService{

    /**
     * uuid 기반으로 member 반환
     *
     * @param memberId
     * @return
     */
    override fun findMemberById(memberId: Long): ResponseMemberDto {
        val member = memberRepository.findById(memberId)
            .orElseThrow{IllegalArgumentException("invalid member id : $memberId")}

        return memberMapper.toDto(member)
    }

    /**
     * email 기반으로 member 반환
     *
     * @param email
     * @return
     */
    override fun findMemberByEmail(email: String): ResponseMemberDto {
        val member = memberRepository.findByEmail(email)
            .orElseThrow{IllegalArgumentException("invalid member email : $email")}

        return memberMapper.toDto(member)
    }

    /**
     * member 생성
     *
     * @param requestCreateMemberDto
     * @return
     */
    override fun createMember(requestCreateMemberDto: RequestCreateMemberDto) : ResponseMemberDto{
        val savedMember = memberMapper.toEntity(requestCreateMemberDto)
        val member = memberRepository.save(savedMember)
        return memberMapper.toDto(member);
    }

    /**
     * 사용자 정보 업데이트
     *
     * @param requestUpdateMemberDto
     * @return
     */
    override fun updateMember(requestUpdateMemberDto: RequestUpdateMemberDto): ResponseMemberDto {
        val memberId = requestUpdateMemberDto.memberId
        val member = memberRepository.findById(memberId)
            .orElseThrow {IllegalArgumentException("invalid member id : $memberId") }

        val fields = member::class.memberProperties  //dto가 가진 필드들 가져옴

        //dto의 필드들을 loop로 순회
        for (field in fields) {
            val fieldName = field.name  //필드의 이름
            val fieldValue = field.getter.call(requestUpdateMemberDto)//필드가 가진 value

            if(fieldValue != null){  //만약 업데이트 안 하려는 필드라면 null로 입력됨 -> 로직 동작x
                when(fieldName){
                    "name" -> member.name = fieldValue as String
                    "profileImageUrl" -> member.profileImageUrl = fieldValue as String?
                }
            }
        }

        return memberMapper.toDto(member)
    }

    /**
     * member 삭제
     *
     * @param memberId
     */
    override fun deleteMember(memberId: Long) {
        memberRepository.deleteById(memberId);
    }

    override fun loadUserByUsername(username: String): UserDetails {
        return memberRepository.findByUsername(username)
            .orElseThrow { IllegalArgumentException("invalid user name : $username") }
    }
}