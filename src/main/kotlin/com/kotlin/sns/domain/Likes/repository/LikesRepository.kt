package com.kotlin.sns.domain.Likes.repository

import com.kotlin.sns.domain.Likes.entity.Likes
import org.springframework.data.jpa.repository.JpaRepository

interface LikesRepository : JpaRepository<Likes, Long> {
}