package com.kotlin.sns.domain.Member.service.Impl

import com.kotlin.sns.common.exception.CustomException
import com.kotlin.sns.common.exception.ExceptionConst
import com.kotlin.sns.domain.Comment.dto.response.ResponseCommentDto
import com.kotlin.sns.domain.Member.dto.request.RequestCreateMemberDto
import com.kotlin.sns.domain.Member.dto.request.RequestUpdateMemberDto
import com.kotlin.sns.domain.Member.dto.response.ResponseMemberDto
import com.kotlin.sns.domain.Member.entity.Member
import com.kotlin.sns.domain.Member.repository.MemberRepository
import com.kotlin.sns.domain.Member.service.MemberService
import com.kotlin.sns.domain.Posting.dto.response.ResponsePostingDto
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

        return createResponseMemberDto(member)
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
     *
     * @param member
     * @return
     */
    private fun createResponseMemberDto(member : Member) : ResponseMemberDto{
        val postingList = member.postings?.map { posting -> ResponsePostingDto(
            postingId = posting.id,
            writerId = member.id,
            writerName = member.name,
            content = posting.content,
            imageUrl = posting.imageInPosting.map { it.imageUrl },
            hashTagList = posting.postingHashtag.map { it.hashtag.tagName  }
            ) } ?: emptyList()

        val commentList = member.comments?.map { comment -> ResponseCommentDto(
            writerId = member.id,
            writerName = member.name,
            content = comment.content,
            createDt = comment.createdDt,
            updateDt = comment.updateDt
        )}

        return ResponseMemberDto(
            name = member.name,
            profileImage = member.profileImageUrl?.imageUrl,
            uploadedPostingList = postingList,
            uploadedPostingCnt = postingList.size
        )
    }
}
