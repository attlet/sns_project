package com.kotlin.sns.domain.Posting.service

import com.kotlin.sns.domain.Posting.entity.Posting
import org.springframework.data.jpa.repository.JpaRepository

interface PostingService {
    fun findPostingById()
    fun createPosting()
    fun updatePosting()
    fun deletePosting()
}