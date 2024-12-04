package com.kotlin.sns.domain.Hashtag.service

import com.kotlin.sns.domain.Hashtag.dto.request.RequestHashtagDto
import com.kotlin.sns.domain.Hashtag.dto.response.ResponseHashtagDto

interface HashtagService {
    fun createHashtag(requestHashtagDto: RequestHashtagDto) : ResponseHashtagDto
    fun deleteHashtag(hashtagId : Long)
}