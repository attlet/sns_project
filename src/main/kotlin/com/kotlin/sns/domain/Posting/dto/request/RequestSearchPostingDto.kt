package com.kotlin.sns.domain.Posting.dto.request

data class RequestSearchPostingDto(
    val writerName : String? = null,
    val hashtagList : List<String>? = null
) {
}