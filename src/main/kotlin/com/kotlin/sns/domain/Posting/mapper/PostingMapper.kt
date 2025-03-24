package com.kotlin.sns.domain.Posting.mapper

import com.kotlin.sns.domain.Member.entity.Member
import com.kotlin.sns.domain.Posting.dto.response.ResponsePostingDto
import com.kotlin.sns.domain.Posting.entity.Posting

object PostingMapper {
    fun toDto(posting : Posting, member : Member) : ResponsePostingDto {
        return ResponsePostingDto(
            postingId = posting.id,
            writerId = member.id,
            writerName = member.name,
            content = posting.content,
            imageUrl = posting.imageInPosting?.map { it.imageUrl },
            hashTagList = posting.postingHashtag.map { it.hashtag.tagName }
        )
    }
}