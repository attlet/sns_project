package com.kotlin.sns.domain.Posting.mapper

import org.mapstruct.Mapper

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