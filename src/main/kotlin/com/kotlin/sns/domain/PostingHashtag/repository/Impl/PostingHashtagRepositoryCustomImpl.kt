package com.kotlin.sns.domain.PostingHashtag.repository.Impl

import com.kotlin.sns.domain.PostingHashtag.entity.QPostingHashtag
import com.kotlin.sns.domain.PostingHashtag.repository.PostingHashtagRepositoryCustom
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class PostingHashtagRepositoryCustomImpl(
    private val jpaQueryFactory: JPAQueryFactory
): PostingHashtagRepositoryCustom {

    private val qPostingHashtag = QPostingHashtag.postingHashtag
    override fun deleteByPostingId(postingId: Long) {
        jpaQueryFactory.delete(qPostingHashtag)
            .where(qPostingHashtag.posting.id.eq(postingId))
    }
}