package com.kotlin.sns.domain.Posting.repository.Impl

import com.kotlin.sns.domain.Comment.entity.QComment
import com.kotlin.sns.domain.Hashtag.entity.QHashtag
import com.kotlin.sns.domain.Image.entity.QImage
import com.kotlin.sns.domain.Member.entity.QMember
import com.kotlin.sns.domain.Posting.dto.request.RequestSearchPostingDto
import com.kotlin.sns.domain.Posting.dto.response.ResponsePostingDto
import com.kotlin.sns.domain.Posting.entity.Posting
import com.kotlin.sns.domain.Posting.entity.QPosting
import com.kotlin.sns.domain.Posting.repository.PostingRepositoryCustom
import com.kotlin.sns.domain.PostingHashtag.entity.QPostingHashtag
import com.querydsl.core.BooleanBuilder
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
    private val qHashtag = QHashtag.hashtag
    private val qPostingHashtag = QPostingHashtag.postingHashtag

    /**
     * 게시글 하나 상세 조회
     * 첨부 이미지, 댓글, 댓글 작성자 같이 조회
     *
     * @param postingId
     * @return
     */
    override fun findByIdForDetail(postingId: Long): Optional<Posting> {
        val posting =  jpaQueryFactory
            .selectFrom(qPosting)
            .leftJoin(qPosting.imageInPosting, qImage)
            .leftJoin(qPosting.postingHashtag, qPostingHashtag)
            .leftJoin(qPosting.comment, qComment).fetchJoin()
            .leftJoin(qComment.member, qMember).fetchJoin()
            .where(qPosting.id.eq(postingId))
            .fetchOne()

        return Optional.ofNullable(posting)
    }

    /**
     * 게시글 리스트 조회
     *
     * @param pageable
     * @return
     */
    override fun findPostingList(pageable: Pageable, requestSearchPostingDto: RequestSearchPostingDto): List<Posting> {
        val builder = searchCondition(requestSearchPostingDto)

        return jpaQueryFactory
            .selectFrom(qPosting)
            .leftJoin(qPosting.imageInPosting, qImage)
            .leftJoin(qPosting.postingHashtag, qPostingHashtag)
            .leftJoin(qPostingHashtag.hashtag, qHashtag)
            .where(builder)
            .orderBy(qPosting.createdDt.desc())
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()
    }

    /**
     * posting 검색 조건들
     *
     * @param requestSearchPostingDto
     * @return
     */
    private fun searchCondition(requestSearchPostingDto: RequestSearchPostingDto) : BooleanBuilder{
        val builder = BooleanBuilder()

        requestSearchPostingDto.writerName?.let { builder.and(qPosting.member.name.containsIgnoreCase(it)) }
        requestSearchPostingDto.hashtagList?.let { builder.and(qPosting.postingHashtag.any().hashtag.tagName.`in`(it))}

        return builder
    }
}