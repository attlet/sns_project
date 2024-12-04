package com.kotlin.sns.domain.Hashtag.repository

import com.kotlin.sns.domain.Hashtag.entity.Hashtag
import org.springframework.data.jpa.repository.JpaRepository

interface HashtagRepository : JpaRepository<Hashtag, Long> , HashtagRepositoryCustom{
}