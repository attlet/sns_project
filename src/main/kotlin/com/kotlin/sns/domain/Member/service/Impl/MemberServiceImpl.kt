package com.kotlin.sns.domain.Member.service.Impl

import com.kotlin.sns.common.exception.CustomException
import com.kotlin.sns.common.exception.ExceptionConst
import com.kotlin.sns.domain.Comment.dto.response.ResponseCommentDto
import com.kotlin.sns.domain.Member.dto.request.RequestCreateMemberDto
import com.kotlin.sns.domain.Member.dto.request.RequestUpdateMemberDto
import com.kotlin.sns.domain.Member.dto.response.ResponseMemberDto
import com.kotlin.sns.domain.Member.entity.Member
import com.kotlin.sns.domain.Member.mapper.MemberMapper
import com.kotlin.sns.domain.Member.repository.MemberRepository
import com.kotlin.sns.domain.Member.service.MemberService
import com.kotlin.sns.domain.Posting.dto.request.RequestSearchPostingDto
import com.kotlin.sns.domain.Posting.dto.response.ResponsePostingDto
import com.kotlin.sns.domain.Posting.entity.Posting
import com.kotlin.sns.domain.Posting.mapper.PostingMapper
import com.kotlin.sns.domain.Posting.repository.PostingRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.awt.print.Pageable

/**
 * member의 비즈니스 로직 처리
 *
 * @property memberRepository
 * @property memberMapper
 */
@Service
class MemberServiceImpl(
    private val memberRepository: MemberRepository,
    private val postingRepository: PostingRepository
) : MemberService, UserDetailsService {

    private val logging = KotlinLogging.logger{}

    /**
     * uuid 기반으로 member 반환
     * 해당 member의 공개할 모든 정보를 조회
     * 사용될지 여부 확인 필요
     *
     * @param memberId
     * @return
     */
    @Transactional(readOnly = true)
    override fun findMemberById(memberId: Long): ResponseMemberDto {
        logging.info { "memberService findByMemberId : memberId : $memberId" }

        val member = memberRepository.findById(memberId)
            .orElseThrow {
                CustomException(
                    ExceptionConst.MEMBER,
                    HttpStatus.NOT_FOUND,
                    "Member with id $memberId not found"
                )
            }

        return createResponseMemberDto(member)
    }

    /**
     * email 기반으로 member 반환
     *
     * @param email
     * @return
     */
    @Transactional(readOnly = true)
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

        return createResponseMemberDto(member)
    }

    /**
     * user id 기반으로 사용자 상세 조회
     * 해당 사용자가 작성한 포스팅들도 같이 조회
     *
     * @param userId
     * @return
     */
    @Transactional(readOnly = true)
    override fun findMemberByUserId(userId: String): ResponseMemberDto {
        logging.info{"memberService findMemberByUserId"}

        val member = memberRepository.findByUserId(userId)
            .orElseThrow {
                CustomException(
                    ExceptionConst.MEMBER,
                    HttpStatus.NOT_FOUND,
                    "Member with userid $userId not found"
                )
            }

        val pageable = PageRequest.of(0, 10)
        val requestSearchPostingDto = RequestSearchPostingDto(
            writerName = member.name
        )

        val postingList = postingRepository.findPostingList(pageable, requestSearchPostingDto)

        return createResponseMemberDto(member, postingList)
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

        val savedMember = Member(
            userId = requestCreateMemberDto.userId,
            name = requestCreateMemberDto.name,
            email = requestCreateMemberDto.email,
            pw = requestCreateMemberDto.pw,
            roles = listOf("user"))

        val member = memberRepository.save(savedMember)
        return createResponseMemberDto(member)
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

        return createResponseMemberDto(updateMember)
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

    /**
     * responseMemberDto 생성하는 메서드
     * member가 작성한 게시글들을 반환해야한다면, postingList 매개변수 사용 필요.
     *
     * @param member
     * @return
     */
    private fun createResponseMemberDto(member : Member, postingList : List<Posting>? = null) : ResponseMemberDto{
        val postingList = postingList?.map { it -> PostingMapper.toDto(it, member)}

        return MemberMapper.toDto(member, postingList)
    }
}
