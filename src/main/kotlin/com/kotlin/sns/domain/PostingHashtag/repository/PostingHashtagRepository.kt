package com.kotlin.sns.domain.PostingHashtag.repository

import com.kotlin.sns.domain.PostingHashtag.entity.PostingHashtag
import org.springframework.data.jpa.repository.JpaRepository

interface PostingHashtagRepository : JpaRepository<PostingHashtag, Long>, PostingHashtagRepositoryCustom {
}