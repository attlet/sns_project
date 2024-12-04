package com.kotlin.sns.domain.Likes.service

import com.kotlin.sns.domain.Likes.dto.request.RequestLikesDto

interface LikesService {
    fun createLikes(requestLikesDto: RequestLikesDto) : Int
    fun cancleLikes(requestLikesDto: RequestLikesDto) : Int
}