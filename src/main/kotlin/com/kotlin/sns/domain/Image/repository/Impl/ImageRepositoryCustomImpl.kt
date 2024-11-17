package com.kotlin.sns.domain.Image.repository.Impl

import com.kotlin.sns.domain.Image.entity.Image
import com.kotlin.sns.domain.Image.entity.QImage
import com.kotlin.sns.domain.Image.repository.ImageRepositoryCustom
import com.querydsl.jpa.impl.JPAQueryFactory

class ImageRepositoryCustomImpl(
    private val jpaQueryFactory: JPAQueryFactory
) : ImageRepositoryCustom{

    private val qImage = QImage.image
    override fun findAllByPostingId(postingId: Long) : List<Image>?{
        return jpaQueryFactory.selectFrom(qImage)
            .where(qImage.posting.id.eq(postingId))
            .fetch()
    }

    override fun deleteAllByPostingId(postingId: Long) {
        jpaQueryFactory.delete(qImage)
            .where(qImage.posting.id.eq(postingId))
    }
}