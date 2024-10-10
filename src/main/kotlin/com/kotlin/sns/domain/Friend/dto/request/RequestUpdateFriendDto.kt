package com.kotlin.sns.domain.Friend.dto.request

import com.kotlin.sns.domain.Friend.const.friendApplyStatusEnum

class RequestUpdateFriendDto(
    val senderId : Long,
    val receiverId : Long,
    val status : friendApplyStatusEnum
) {
}