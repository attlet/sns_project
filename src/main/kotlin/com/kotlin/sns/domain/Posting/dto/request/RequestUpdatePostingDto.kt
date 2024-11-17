package com.kotlin.sns.domain.Posting.dto.request

import org.springframework.web.multipart.MultipartFile

data class RequestUpdatePostingDto(
    var postingId : Long,
    var content: String? = null,
    var imageUrl : List<MultipartFile>? = null
) {
}