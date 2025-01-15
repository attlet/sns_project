package com.kotlin.sns.domain.Friend.dto.request

import com.kotlin.sns.domain.Friend.const.friendApplyStatusEnum

data class RequestUpdateFriendDto(
    val friendRequestId : Long,
    val senderId : Long,
    val receiverId : Long,
    val status : friendApplyStatusEnum
) {
}