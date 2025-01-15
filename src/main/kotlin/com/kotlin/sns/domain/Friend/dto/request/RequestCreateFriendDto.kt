package com.kotlin.sns.domain.Friend.dto.request

data class RequestCreateFriendDto(
    val senderId : Long,
    val receiverId : Long
) {
}