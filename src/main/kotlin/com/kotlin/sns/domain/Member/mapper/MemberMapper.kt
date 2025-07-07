package com.kotlin.sns.domain.Member.mapper

import com.kotlin.sns.domain.Member.dto.response.ResponseMemberDto
import com.kotlin.sns.domain.Member.entity.Member
import com.kotlin.sns.domain.Posting.dto.response.ResponsePostingDto

/**
 * member entity <-> dto 간 변환 메서드 모아두는 object
 */
object MemberMapper {
    fun toDto(member : Member, postingList : List<ResponsePostingDto>?) : ResponseMemberDto{
        return ResponseMemberDto(
            name = member.name,
            userId = member.userId,
            profileImage = member?.profileImageUrl?.imageUrl,
            uploadedPostingList = postingList,
            uploadedPostingCnt = postingList?.size
        )
    }
}