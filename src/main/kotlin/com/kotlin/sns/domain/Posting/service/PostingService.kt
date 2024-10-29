package com.kotlin.sns.domain.Posting.service

import com.kotlin.sns.domain.Posting.dto.request.RequestCreatePostingDto
import com.kotlin.sns.domain.Posting.dto.request.RequestUpdatePostingDto
import com.kotlin.sns.domain.Posting.dto.response.ResponsePostingDto
import com.kotlin.sns.domain.Posting.entity.Posting
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface PostingService {
    fun findPostingById(postingId : Long) : ResponsePostingDto
    fun findPostingList(pageable: Pageable) : List<ResponsePostingDto>
    fun createPosting(requestCreaetePostingDto: RequestCreatePostingDto) : ResponsePostingDto
    fun updatePosting(requestUpdatePostingDto: RequestUpdatePostingDto) : ResponsePostingDto
    fun deletePosting(postingId : Long)
}