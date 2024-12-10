package com.kotlin.sns.domain.Posting.dto.request

data class RequestSearchPostingDto(
    val writerId : Long? = null,
    val postingTitle : String? =null,
    val hashtagList : List<String>? = emptyList()
) {
}