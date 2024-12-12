package com.kotlin.sns.domain.Posting.repository

import com.kotlin.sns.domain.Posting.dto.request.RequestSearchPostingDto
import com.kotlin.sns.domain.Posting.entity.Posting
import org.springframework.data.domain.Pageable
import java.util.Optional

interface PostingRepositoryCustom {
    fun findByIdForDetail(postingId: Long) : Optional<Posting>
    fun findPostingList(pageable: Pageable, requestSearchPostingDto: RequestSearchPostingDto) : List<Posting>
}