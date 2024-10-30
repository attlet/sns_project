package com.kotlin.sns.domain.Posting.repository

import com.kotlin.sns.domain.Posting.entity.Posting
import org.springframework.data.domain.Pageable

interface PostingRepositoryCustom {
    fun getPostingList(pageable: Pageable) : List<Posting>;
}