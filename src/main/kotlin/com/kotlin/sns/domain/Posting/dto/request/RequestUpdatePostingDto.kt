package com.kotlin.sns.domain.Posting.dto.request

import org.springframework.web.multipart.MultipartFile

data class RequestUpdatePostingDto(
    val postingId : Long,
    val content: String? = null,
    val imageUrl : List<MultipartFile>? = null,
    val hashTagList : List<String>? = null
) {
}