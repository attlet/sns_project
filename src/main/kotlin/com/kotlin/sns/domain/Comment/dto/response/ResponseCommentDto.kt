package com.kotlin.sns.domain.Comment.dto.response

import java.time.Instant

data class ResponseCommentDto(
    val writerId : Long,
    val writerName : String,
    val content : String,
    val createDt : Instant,
    val updateDt : Instant
) {
}