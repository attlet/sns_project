package com.kotlin.sns.domain.Likes.repository.Impl

import com.kotlin.sns.domain.Likes.dto.request.RequestLikesDto
import com.kotlin.sns.domain.Likes.entity.Likes
import com.kotlin.sns.domain.Likes.entity.QLikes
import com.kotlin.sns.domain.Likes.repository.LikesRepositoryCustom
import com.kotlin.sns.domain.Posting.entity.QPosting
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class LikesRepositoryCustomImpl(
    private val jpaQueryFactory: JPAQueryFactory
) : LikesRepositoryCustom {

    private val qLikes = QLikes.likes
    private val qPosting = QPosting.posting

    override fun findLikesByMemberAndPosting(requestLikesDto: RequestLikesDto): Optional<Likes> {
        val memberId = requestLikesDto.memberId
        val postingId = requestLikesDto.postingId

        val query =  jpaQueryFactory
            .selectFrom(qLikes)
            .where(qLikes.member.id.eq(memberId)
                .and(qLikes.posting.id.eq(postingId)))
            .fetchOne()

        return Optional.ofNullable(query)
    }

    override fun getLikesCount(postingId: Long): Int {
        return jpaQueryFactory
            .select(qLikes.count())
            .from(qLikes)
            .where(qLikes.posting.id.eq(postingId))
            .fetchOne()?.toInt() ?:0
    }

    override fun checkLikesExist(requestLikesDto: RequestLikesDto): Boolean {
        val memberId = requestLikesDto.memberId
        val postingId = requestLikesDto.postingId

        return jpaQueryFactory
            .selectFrom(qLikes)
            .where(qLikes.member.id.eq(memberId)
                .and(qLikes.posting.id.eq(postingId)))
            .fetchFirst() != null
    }
}