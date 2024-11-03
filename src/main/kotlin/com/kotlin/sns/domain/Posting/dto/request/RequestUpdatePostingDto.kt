package com.kotlin.sns.domain.Posting.dto.request

data class RequestUpdatePostingDto(
    var postingId : Long,
    var content: String,
    var imageUrl : String?
) {
}