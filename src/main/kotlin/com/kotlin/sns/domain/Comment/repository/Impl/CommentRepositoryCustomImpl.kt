package com.kotlin.sns.domain.Comment.repository.Impl


import com.kotlin.sns.domain.Comment.entity.Comment
import com.kotlin.sns.domain.Comment.entity.QComment
import com.kotlin.sns.domain.Comment.repository.CommentRepositoryCustom
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Pageable

import org.springframework.stereotype.Repository

@Repository
class CommentRepositoryCustomImpl(
    private val jpaQueryFactory: JPAQueryFactory
) : CommentRepositoryCustom{

    private val qComment = QComment.comment

    /**
     * 해당 포스팅의 댓글들 조회
     *
     * @param postingId
     * @return
     */
    override fun findCommentListByPosting(pageable: Pageable, postingId: Long): List<Comment> {
        return jpaQueryFactory
            .selectFrom(qComment)
            .where(qComment.posting.id.eq(postingId))
            .orderBy(qComment.createdDt.desc())
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()
    }
}