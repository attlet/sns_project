package com.kotlin.sns.domain.Comment.repository

import com.kotlin.sns.domain.Comment.entity.Comment
import org.springframework.data.domain.Pageable


interface CommentRepositoryCustom {
    fun findCommentListByPosting(pageable: Pageable, postingId:Long) : List<Comment>
}