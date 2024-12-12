package com.kotlin.sns.domain.Posting.dto.response

import com.kotlin.sns.domain.Comment.dto.response.ResponseCommentDto

data class ResponsePostingDto(
    val writerId : Long,
    val writerName : String,
    val content : String,
    val imageUrl : List<String>? = null,
    val hashTagList : List<String>? = null
) {
}