package com.kotlin.sns.domain.PostingHashtag.service

import com.kotlin.sns.domain.PostingHashtag.dto.request.RequestPostingHashtagDto
import com.kotlin.sns.domain.PostingHashtag.dto.response.ResponsePostingHashtagDto

interface PostingHashtagService {
    fun createPostingHashtag(requestPostingHashtagDtoList: List<RequestPostingHashtagDto>) : List<ResponsePostingHashtagDto>

}