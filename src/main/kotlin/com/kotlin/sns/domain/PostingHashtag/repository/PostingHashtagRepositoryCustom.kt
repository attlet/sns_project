package com.kotlin.sns.domain.PostingHashtag.repository

interface PostingHashtagRepositoryCustom {
    fun deleteByPostingId(postingId : Long)
}