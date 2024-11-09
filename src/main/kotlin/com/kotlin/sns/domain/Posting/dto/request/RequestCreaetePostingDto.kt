package com.kotlin.sns.domain.Posting.dto.request

import org.springframework.web.multipart.MultipartFile

data class RequestCreatePostingDto (
    val writerId : Long,
    val content : String,
    val imageUrl : List<MultipartFile>? = null
){
}