package com.kotlin.sns.domain.Hashtag.repository.Impl

import com.kotlin.sns.domain.Hashtag.entity.Hashtag
import com.kotlin.sns.domain.Hashtag.entity.QHashtag
import com.kotlin.sns.domain.Hashtag.repository.HashtagRepositoryCustom
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class HashtagRepositoryCustomImpl(
    private val jpaQueryFactory: JPAQueryFactory
) : HashtagRepositoryCustom {

    private val qHashtag = QHashtag.hashtag

    /**
     * 이전에 생성된 적 있는 hashtag인지 확인
     *
     * @param tagNames
     * @return
     */
    override fun findByTagNameForExist(tagNames: List<String>): List<Hashtag> {
         return jpaQueryFactory
             .selectFrom(qHashtag)
             .where(qHashtag.tagName.`in`(tagNames))
             .fetch()
    }
}