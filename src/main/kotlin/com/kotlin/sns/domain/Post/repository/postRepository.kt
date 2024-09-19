package com.kotlin.sns.domain.Post.repository

import com.kotlin.sns.domain.Post.entity.Post
import org.springframework.data.jpa.repository.JpaRepository

interface postRepository : JpaRepository<Post, Long> {

}