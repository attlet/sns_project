package com.kotlin.sns.domain.Posting.repository.Impl

import com.kotlin.sns.domain.Comment.entity.QComment
import com.kotlin.sns.domain.Image.entity.QImage
import com.kotlin.sns.domain.Member.entity.QMember
import com.kotlin.sns.domain.Posting.dto.response.ResponsePostingDto
import com.kotlin.sns.domain.Posting.entity.Posting
import com.kotlin.sns.domain.Posting.entity.QPosting
import com.kotlin.sns.domain.Posting.repository.PostingRepositoryCustom
import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Pageable
import java.util.Optional

class PostingRepositoryCustomImpl(
    private val jpaQueryFactory: JPAQueryFactory
) : PostingRepositoryCustom {

    private val qPosting = QPosting.posting
    private val qComment = QComment.comment
    private val qMember = QMember.member
    private val qImage = QImage.image
    override fun findByIdForDetail(postingId: Long): Optional<Posting> {
        val posting =  jpaQueryFactory
            .selectFrom(qPosting)
            .leftJoin(qPosting.imageInPosting, qImage).fetchJoin()
            .leftJoin(qPosting.comment, qComment).fetchJoin()
            .leftJoin(qComment.member, qMember).fetchJoin()
            .where(qPosting.id.eq(postingId))
            .fetchOne()

        return Optional.ofNullable(posting)
    }

    override fun getPostingListWithComment(pageable: Pageable): List<Posting> {
        return jpaQueryFactory
            .selectFrom(qPosting)
            .join(qPosting.imageInPosting, qImage).fetchJoin()
            .join(qPosting.comment, qComment).fetchJoin()
            .join(qComment.member, qMember).fetchJoin()
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