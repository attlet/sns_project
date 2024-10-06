package com.kotlin.sns.domain.Posting.dto.request

data class RequestCreaetePostingDto (
    val content : String,
    val imageUrl : String? = null,

){
}