package com.kotlin.sns.domain.Friend.dto.response

import com.kotlin.sns.domain.Friend.const.friendApplyStatusEnum

class ResponseFriendDto(
    val senderId : Long,
    val receiverId : Long,
    val status : friendApplyStatusEnum
) {
}