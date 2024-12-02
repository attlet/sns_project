package com.kotlin.sns.domain.Likes.repository

import com.kotlin.sns.domain.Likes.dto.request.RequestLikesDto
import com.kotlin.sns.domain.Likes.entity.Likes
import java.util.Optional

interface LikesRepositoryCustom {
    fun findLikesByMemberAndPosting(requestLikesDto: RequestLikesDto) : Optional<Likes>
    fun getLikesCount(postingId : Long) : Int
    fun checkLikesExist(requestLikesDto: RequestLikesDto) : Boolean
}