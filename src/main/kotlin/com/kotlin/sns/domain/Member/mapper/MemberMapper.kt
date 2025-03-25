package com.kotlin.sns.domain.Member.mapper

import com.kotlin.sns.domain.Member.dto.response.ResponseMemberDto
import com.kotlin.sns.domain.Member.entity.Member
import com.kotlin.sns.domain.Posting.dto.response.ResponsePostingDto


object MemberMapper {
    fun toDto(member : Member, postingList : List<ResponsePostingDto>?) : ResponseMemberDto{
        return ResponseMemberDto(
            name = member.name,
            profileImage = member?.profileImageUrl?.imageUrl,
            uploadedPostingList = postingList,
            uploadedPostingCnt = postingList?.size
        )
    }
}