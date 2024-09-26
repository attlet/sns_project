package com.kotlin.sns.domain.Post.repository

import com.kotlin.sns.domain.Post.entity.Posting
import org.springframework.data.jpa.repository.JpaRepository

interface PostingRepository : JpaRepository<Posting, Long> {

}