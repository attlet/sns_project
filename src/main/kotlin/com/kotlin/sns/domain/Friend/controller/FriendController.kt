package com.kotlin.sns.domain.Friend.controller

import com.kotlin.sns.domain.Friend.dto.request.RequestCreateFriendDto
import com.kotlin.sns.domain.Friend.dto.request.RequestUpdateFriendDto
import com.kotlin.sns.domain.Friend.dto.response.ResponseFriendDto
import com.kotlin.sns.domain.Friend.service.FriendService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * friend controller
 *
 * @property friendService
 */
@RestController
@RequestMapping("/friends")
class FriendController(
    private val friendService: FriendService
) {
    @GetMapping
    fun getFriendById(@RequestParam("friendId") friendId : Long) : ResponseFriendDto{
        return friendService.findFriendById(friendId)
    }

    @PostMapping
    fun sendFriend(@RequestBody requestCreateFriendDto: RequestCreateFriendDto) : ResponseFriendDto{
        return friendService.sendFriend(requestCreateFriendDto)
    }

    @PutMapping
    fun updateFriend(@RequestBody requestUpdateFriendDto: RequestUpdateFriendDto) : ResponseFriendDto{
        return friendService.updateFriend(requestUpdateFriendDto)
    }
    @DeleteMapping
    fun deleteFriend(@RequestParam("friendId") friendId: Long){
        friendService.deleteFriend(friendId)
    }
}