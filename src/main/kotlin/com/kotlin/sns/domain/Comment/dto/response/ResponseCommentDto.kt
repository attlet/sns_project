package com.kotlin.sns.domain.Comment.dto.response

import java.time.Instant

data class ResponseCommentDto(
    val content : String,
    val createAt : Instant,
    val updateAt : Instant
) {
}