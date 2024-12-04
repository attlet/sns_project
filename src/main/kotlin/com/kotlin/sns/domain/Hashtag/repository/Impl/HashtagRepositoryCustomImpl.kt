package com.kotlin.sns.domain.Hashtag.repository.Impl

import com.kotlin.sns.domain.Hashtag.repository.HashtagRepositoryCustom
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class HashtagRepositoryCustomImpl(
    private val jpaQueryFactory: JPAQueryFactory
) : HashtagRepositoryCustom {

//    private val qHashtag = QHash
    override fun findByTagNameForNotExist(tagNames: List<String>): List<String> {
        return emptyList()

    }
}