package com.kotlin.sns.domain.Friend.service

import com.kotlin.sns.domain.Friend.dto.request.RequestCreateFriendDto
import com.kotlin.sns.domain.Friend.dto.request.RequestUpdateFriendDto
import com.kotlin.sns.domain.Friend.dto.response.ResponseFriendDto
import org.apache.coyote.Response

interface FriendService {
    fun findFriendById(friendId : Long) : ResponseFriendDto
    fun sendFriend(requestCreateFriendDto: RequestCreateFriendDto) : ResponseFriendDto
    fun updateFriend(requestUpdateFriendDto: RequestUpdateFriendDto) : ResponseFriendDto
    fun deleteFriend(friendId: Long)
}