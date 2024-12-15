package com.kotlin.sns.domain.Comment.repository

import com.kotlin.sns.domain.Comment.entity.Comment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CommentRepository : JpaRepository<Comment, Long>, CommentRepositoryCustom{
}