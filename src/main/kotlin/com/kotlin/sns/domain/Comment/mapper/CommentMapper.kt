package com.kotlin.sns.domain.Comment.mapper

import com.kotlin.sns.domain.Comment.dto.request.RequestCommentDto
import com.kotlin.sns.domain.Comment.dto.response.ResponseCommentDto
import com.kotlin.sns.domain.Comment.entity.Comment
import org.mapstruct.Mapper
import org.mapstruct.Mapping


@Mapper(componentModel = "spring")
interface CommentMapper {
//
//    fun toEntity(requestCommentDto: RequestCommentDto) : Comment
//
//    @Mapping(source = "member.id", target = "writerId")
//    @Mapping(source = "member.name", target = "writerName")
//    fun toDto(comment: Comment) : ResponseCommentDto
}