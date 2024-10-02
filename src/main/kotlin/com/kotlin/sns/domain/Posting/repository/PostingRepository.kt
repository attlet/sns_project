package com.kotlin.sns.domain.Posting.repository

import com.kotlin.sns.domain.Posting.entity.Posting
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PostingRepository : JpaRepository<Posting, Long> {

}