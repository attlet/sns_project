package com.kotlin.sns.domain.Posting.dto.request

data class RequestCreatePostingDto (
    val writerId : Long,
    val content : String,
    val imageUrl : String? = null,

){
}