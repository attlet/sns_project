package com.kotlin.sns.domain.Friend.dto.response

import com.kotlin.sns.domain.Friend.const.friendApplyStatusEnum

data class ResponseFriendDto(
    val friendRequestId : Long,
    val senderId : Long,
    val receiverId : Long,
    val status : friendApplyStatusEnum
) {
}