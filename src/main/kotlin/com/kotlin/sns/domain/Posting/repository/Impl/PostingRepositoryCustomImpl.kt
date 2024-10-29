package com.kotlin.sns.domain.Posting.repository.Impl

import com.kotlin.sns.domain.Posting.entity.Posting
import com.kotlin.sns.domain.Posting.entity.QPosting
import com.kotlin.sns.domain.Posting.repository.PostingRepositoryCustom
import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Pageable

class PostingRepositoryCustomImpl(
    private val jpaQueryFactory: JPAQueryFactory
) : PostingRepositoryCustom {

    private val qPosting = QPosting.posting
    override fun getPostingList(pageable: Pageable): List<Posting> {
        return jpaQueryFactory
            .selectFrom(qPosting)
//            .where()
            .orderBy(qPosting.createdDt.desc())
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

    }

    fun condition(){
        //포스팅 필터링 조건
    }
}