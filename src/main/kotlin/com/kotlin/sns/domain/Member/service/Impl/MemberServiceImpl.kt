package com.kotlin.sns.domain.Member.service.Impl

import com.kotlin.sns.common.exception.CustomException
import com.kotlin.sns.common.exception.ExceptionConst
import com.kotlin.sns.domain.Image.entity.Image
import com.kotlin.sns.domain.Image.entity.ImageType
import com.kotlin.sns.domain.Image.service.ImageService
import com.kotlin.sns.domain.Member.dto.request.RequestCreateMemberDto
import com.kotlin.sns.domain.Member.dto.request.RequestUpdateMemberDto
import com.kotlin.sns.domain.Member.dto.response.ResponseMemberDto
import com.kotlin.sns.domain.Member.mapper.MemberMapper
import com.kotlin.sns.domain.Member.repository.MemberRepository
import com.kotlin.sns.domain.Member.service.MemberService
import org.springframework.http.HttpStatus
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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
    private val memberMapper: MemberMapper,
    private val imageService: ImageService
) : MemberService, UserDetailsService {

    /**
     * uuid 기반으로 member 반환
     *
     * @param memberId
     * @return
     */
    override fun findMemberById(memberId: Long): ResponseMemberDto {
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
        val updateId = requestUpdateMemberDto.memberId
        val updateMember = memberRepository.findById(updateId)
            .orElseThrow {
                CustomException(
                    ExceptionConst.MEMBER,
                    HttpStatus.NOT_FOUND,
                    "Member with id $updateId not found"
                )
            }

        val fields = updateMember::class.memberProperties // dto가 가진 필드들 가져옴

        // dto의 필드들을 loop로 순회
        for (field in fields) {
            val fieldName = field.name // 필드의 이름
            val fieldValue = field.getter.call(requestUpdateMemberDto) // 필드가 가진 value

            if (fieldValue != null) { // 만약 업데이트 안 하려는 필드라면 null로 입력됨 -> 로직 동작x
                when (fieldName) {
                    "name" -> updateMember.name = fieldValue as String
                }
            }
        }

        return memberMapper.toDto(updateMember)
    }

    /**
     * profile 이미지 업데이트
     *
     * @param memberId
     * @param imageUrl
     */
    @Transactional
    override fun updateProfileImage(memberId: Long, imageUrl: String) {
        val member = memberRepository.findById(memberId)
            .orElseThrow {
                CustomException(
                    ExceptionConst.MEMBER,
                    HttpStatus.NOT_FOUND,
                    "Member with id $memberId not found"
                )
            }

        member.profileImageUrl = Image(
            imageUrl = imageUrl,
            imageType = ImageType.PROFILE,
            member = member
        )

        memberRepository.save(member)
    }

    /**
     * member 삭제
     *
     * @param memberId
     */
    @Transactional
    override fun deleteMember(memberId: Long) {
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
