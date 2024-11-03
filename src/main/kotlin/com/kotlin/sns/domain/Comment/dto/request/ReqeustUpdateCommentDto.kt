package com.kotlin.sns.domain.Comment.dto.request

data class ReqeustUpdateCommentDto(
    val commentId : Long,
    val content : String
) {

}