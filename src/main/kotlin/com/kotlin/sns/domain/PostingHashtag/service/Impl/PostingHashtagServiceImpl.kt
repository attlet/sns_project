package com.kotlin.sns.domain.PostingHashtag.service.Impl

import com.kotlin.sns.domain.PostingHashtag.dto.request.RequestPostingHashtagDto
import com.kotlin.sns.domain.PostingHashtag.dto.response.ResponsePostingHashtagDto
import com.kotlin.sns.domain.PostingHashtag.repository.PostingHashtagRepository
import com.kotlin.sns.domain.PostingHashtag.service.PostingHashtagService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PostingHashtagServiceImpl(
    private val postingHashtagRepository: PostingHashtagRepository
) : PostingHashtagService{
    @Transactional
    override fun createPostingHashtag(requestPostingHashtagDtoList: List<RequestPostingHashtagDto>): List<ResponsePostingHashtagDto> {
        return emptyList()
    }
}