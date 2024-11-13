package com.kotlin.sns.domain.Comment.dto.request

data class RequestUpdateCommentDto(
    val commentId : Long,
    val content : String
) {

}