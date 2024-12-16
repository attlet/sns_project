package com.kotlin.sns.domain.Posting.mapper

import com.kotlin.sns.domain.Member.entity.Member
import com.kotlin.sns.domain.Posting.dto.request.RequestCreatePostingDto
import com.kotlin.sns.domain.Posting.dto.response.ResponsePostingDto
import com.kotlin.sns.domain.Posting.entity.Posting
import org.mapstruct.Mapper
import org.mapstruct.Mapping

/**
 * posting <-> dto 변환 mapper
 *
 */
@Mapper(componentModel = "spring")
interface PostingMapper {
//    @Mapping(source = "member", target = "member")
//    fun toEntity(requestCreatePostingDto: RequestCreatePostingDto, member: Member) : Posting
//
//    @Mapping(source = "member.id", target = "writerId")
//    @Mapping(source = "member.name", target = "writerName")
//    @Mapping(source = "comment", target = "commentList")
//    fun toDto(posting: Posting) : ResponsePostingDto
}