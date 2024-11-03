package com.kotlin.sns.domain.Comment.dto.request

data class RequestCommentDto(
    val writerId : Long,
    val postingId : Long,
    val content : String
) {

}