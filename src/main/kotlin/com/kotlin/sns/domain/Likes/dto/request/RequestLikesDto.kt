package com.kotlin.sns.domain.Likes.dto.request


data class RequestLikesDto(
    val memberId : Long,
    val postingId : Long
) {
}