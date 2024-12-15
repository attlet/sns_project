package com.kotlin.sns.domain.Comment.service

import com.kotlin.sns.domain.Comment.dto.request.RequestUpdateCommentDto
import com.kotlin.sns.domain.Comment.dto.request.RequestCommentDto
import com.kotlin.sns.domain.Comment.dto.response.ResponseCommentDto
import org.springframework.data.domain.Pageable

interface CommentService {

    fun getCommentListInPosting(pageable: Pageable, postingId : Long) : List<ResponseCommentDto>?
    fun createComment(requestCommentDto: RequestCommentDto): ResponseCommentDto
    fun updateComment(requestUpdateCommentDto: RequestUpdateCommentDto) : ResponseCommentDto
    fun deleteComment(commentId : Long)
}