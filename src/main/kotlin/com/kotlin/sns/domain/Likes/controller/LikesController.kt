package com.kotlin.sns.domain.Likes.controller

import com.kotlin.sns.domain.Likes.dto.request.RequestLikesDto
import com.kotlin.sns.domain.Likes.service.LikesService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/likes")
class LikesController (
    private val likesService: LikesService
){

    @PostMapping()
    fun createLikes(@RequestBody requestLikesDto : RequestLikesDto) : Int{
        return likesService.createLikes(requestLikesDto)
    }

    @DeleteMapping()
    fun cancleLikes(@RequestBody requestLikesDto: RequestLikesDto) : Int{
        return likesService.cancleLikes(requestLikesDto)
    }
}