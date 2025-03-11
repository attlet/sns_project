package com.kotlin.sns.domain.Member.service.Impl

import com.kotlin.sns.common.exception.CustomException
import com.kotlin.sns.common.exception.ExceptionConst
import com.kotlin.sns.domain.Member.dto.request.RequestCreateMemberDto
import com.kotlin.sns.domain.Member.dto.request.RequestUpdateMemberDto
import com.kotlin.sns.domain.Member.dto.response.ResponseMemberDto
import com.kotlin.sns.domain.Member.mapper.MemberMapper
import com.kotlin.sns.domain.Member.repository.MemberRepository
import com.kotlin.sns.domain.Member.service.MemberService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * member의 비즈니스 로직 처리
 *
 * @property memberRepository
 * @property memberMapper
 */
@Service
class MemberServiceImpl(
    private val memberRepository: MemberRepository,
    private val memberMapper: MemberMapper
) : MemberService, UserDetailsService {

    private val logging = KotlinLogging.logger{}

    /**
     * uuid 기반으로 member 반환
     *
     * @param memberId
     * @return
     */
    override fun findMemberById(memberId: Long): ResponseMemberDto {
        logging.info { "memberService findByMemberId" }
        val member = memberRepository.findById(memberId)
            .orElseThrow {
                CustomException(
                    ExceptionConst.MEMBER,
                    HttpStatus.NOT_FOUND,
                    "Member with id $memberId not found"
                )
            }

        return memberMapper.toDto(member)
    }

    /**
     * email 기반으로 member 반환
     *
     * @param email
     * @return
     */
    override fun findMemberByEmail(email: String): ResponseMemberDto {
        logging.info{"memberService findMemberByEmail"}
        val member = memberRepository.findByEmail(email)
            .orElseThrow {
                CustomException(
                    ExceptionConst.MEMBER,
                    HttpStatus.NOT_FOUND,
                    "Member with email $email not found"
                )
            }

        return memberMapper.toDto(member)
    }

    /**
     * member 생성
     *
     * @param requestCreateMemberDto
     * @return
     */
    @Transactional
    override fun createMember(requestCreateMemberDto: RequestCreateMemberDto): ResponseMemberDto {
        logging.info{"memberService createMember"}
        val savedMember = memberMapper.toEntity(requestCreateMemberDto)
        val member = memberRepository.save(savedMember)
        return memberMapper.toDto(member)
    }

    /**
     * 사용자 정보 업데이트
     *
     * @param requestUpdateMemberDto
     * @return
     */
    @Transactional
    override fun updateMember(requestUpdateMemberDto: RequestUpdateMemberDto): ResponseMemberDto {
        logging.info{"memberService updateMember"}
        val updateId = requestUpdateMemberDto.memberId
        val updateMember = memberRepository.findById(updateId)
            .orElseThrow {
                CustomException(
                    ExceptionConst.MEMBER,
                    HttpStatus.NOT_FOUND,
                    "Member with id $updateId not found"
                )
            }

        requestUpdateMemberDto.name?.let { updateMember.name = it }
        requestUpdateMemberDto.email?.let { updateMember.email = it }
//        requestUpdateMemberDto.pw?.let { updateMember.pw = it }

        return memberMapper.toDto(updateMember)
    }
//
//    /**
//     * profile 이미지 업데이트
//     *
//     * @param memberId
//     * @param imageUrl
//     */
//    @Transactional
//    override fun updateProfileImage(memberId: Long, imageUrl: String) {
//        val member = memberRepository.findById(memberId)
//            .orElseThrow {
//                CustomException(
//                    ExceptionConst.MEMBER,
//                    HttpStatus.NOT_FOUND,
//                    "Member with id $memberId not found"
//                )
//            }
//
//        memberRepository.save(member)
//    }

    /**
     * member 삭제
     *
     * @param memberId
     */
    @Transactional
    override fun deleteMember(memberId: Long) {
        logging.info{"memberService deleteMember"}
        if (!memberRepository.existsById(memberId)) {
            throw CustomException(
                ExceptionConst.MEMBER,
                HttpStatus.NOT_FOUND,
                "Member with id $memberId not found"
            )
        }
        memberRepository.deleteById(memberId)
    }

    override fun loadUserByUsername(username: String): UserDetails {
        logging.info{"userDetailService loadUserByUsername"}
        return memberRepository.findByUserId(username)
            .orElseThrow {
                CustomException(
                    ExceptionConst.MEMBER,
                    HttpStatus.NOT_FOUND,
                    "User with username $username not found"
                )
            }
    }
}
