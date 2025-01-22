package com.kotlin.sns.domain.Friend.const

/*
친구 신청 보낸 후 상대방의 처리에 따른 값.

accept : 친구 신청 수락함
blocked : 친구 신청 거절
pending : 아직 처리 안 함

 */
enum class FriendApplyStatusEnum {
    ACCEPT,
    BLOCKED,
    PENDING


}