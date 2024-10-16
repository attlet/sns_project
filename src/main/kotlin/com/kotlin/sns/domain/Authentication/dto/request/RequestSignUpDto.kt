package com.kotlin.sns.domain.Authentication.dto.request

data class RequestSignUpDto (
    val id : String,
    val name: String,
    val password : String,
    val email : String,
    val roles : String
){
}