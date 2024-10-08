package com.kotlin.sns.domain.Friend.controller

import com.kotlin.sns.domain.Friend.dto.request.RequestCreateFriendDto
import com.kotlin.sns.domain.Friend.dto.response.ResponseFriendDto
import com.kotlin.sns.domain.Friend.service.FriendService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
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

    }

    @PostMapping
    fun createFriend(@RequestBody requestCreateFriendDto: RequestCreateFriendDto) : ResponseFriendDto{

    }

    @DeleteMapping
    fun deleteFriend(@RequestParam("friendId") friendId: Long){

    }
}