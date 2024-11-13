package com.kotlin.sns.domain.Comment.service

import com.kotlin.sns.domain.Comment.dto.request.ReqeustUpdateCommentDto
import com.kotlin.sns.domain.Comment.dto.request.RequestCommentDto
import com.kotlin.sns.domain.Comment.dto.response.ResponseCommentDto

interface CommentService {

    fun reloadCommentInPosting(postingId : Long) : List<ResponseCommentDto>
    fun createComment(requestCommentDto: RequestCommentDto)
    fun updateComment(requestUpdateCommentDto: ReqeustUpdateCommentDto)
    fun deleteComment(commentId : Long)
}