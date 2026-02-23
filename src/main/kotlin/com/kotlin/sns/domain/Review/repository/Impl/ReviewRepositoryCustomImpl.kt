package com.kotlin.sns.domain.Review.repository.Impl

import com.kotlin.sns.domain.Content.entity.QContent
import com.kotlin.sns.domain.Member.entity.QMember
import com.kotlin.sns.domain.Review.entity.QReview
import com.kotlin.sns.domain.Review.entity.Review
import com.kotlin.sns.domain.Review.entity.ReviewStatus
import com.kotlin.sns.domain.Review.repository.ReviewRepositoryCustom
import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.stereotype.Repository

/**
 * Review 커스텀 레포지토리 구현체
 *
 * QueryDSL을 사용하여 동적 쿼리 및 fetchJoin을 제공한다.
 *
 * @property jpaQueryFactory QueryDSL 쿼리 팩토리
 */
@Repository
class ReviewRepositoryCustomImpl(
    private val jpaQueryFactory: JPAQueryFactory
) : ReviewRepositoryCustom {

    private val qReview = QReview.review
    private val qMember = QMember.member
    private val qContent = QContent.content

    /**
     * 특정 Member의 삭제되지 않은 Review 목록 조회 (동적 status 필터, 페이징)
     *
     * status가 null이면 조건을 추가하지 않아 전체 조회한다.
     *
     * @param memberId 회원 ID
     * @param status 리뷰 상태 필터 (null이면 전체)
     * @param pageable 페이징 정보
     * @return 페이징된 Review 목록
     */
    override fun findReviewsByMember(memberId: Long, status: ReviewStatus?, pageable: Pageable): Page<Review> {
        val builder = buildMemberCondition(memberId, status)

        val results = jpaQueryFactory
            .selectFrom(qReview)
            .join(qReview.content, qContent).fetchJoin()
            .where(builder)
            .orderBy(qReview.createdDt.desc())
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        return PageableExecutionUtils.getPage(results, pageable) {
            jpaQueryFactory
                .select(qReview.count())
                .from(qReview)
                .where(builder)
                .fetchOne() ?: 0L
        }
    }

    /**
     * 특정 Content의 삭제되지 않은 Review 목록 조회 (페이징)
     *
     * member를 fetchJoin으로 함께 조회하여 N+1을 방지한다.
     *
     * @param contentId 콘텐츠 ID
     * @param pageable 페이징 정보
     * @return 페이징된 Review 목록
     */
    override fun findReviewsByContent(contentId: Long, pageable: Pageable): Page<Review> {
        val builder = BooleanBuilder()
            .and(qReview.content.id.eq(contentId))
            .and(qReview.isDeleted.isFalse)

        val results = jpaQueryFactory
            .selectFrom(qReview)
            .join(qReview.member, qMember).fetchJoin()
            .where(builder)
            .orderBy(qReview.createdDt.desc())
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        return PageableExecutionUtils.getPage(results, pageable) {
            jpaQueryFactory
                .select(qReview.count())
                .from(qReview)
                .where(builder)
                .fetchOne() ?: 0L
        }
    }

    /**
     * Member 기반 검색 조건 빌더
     *
     * @param memberId 회원 ID
     * @param status 상태 필터 (null이면 조건 미추가)
     * @return BooleanBuilder 동적 조건
     */
    private fun buildMemberCondition(memberId: Long, status: ReviewStatus?): BooleanBuilder {
        val builder = BooleanBuilder()
            .and(qReview.member.id.eq(memberId))
            .and(qReview.isDeleted.isFalse)

        status?.let { builder.and(qReview.status.eq(it)) }

        return builder
    }
}
