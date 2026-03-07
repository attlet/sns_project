package com.kotlin.sns.domain.Review.repository

import com.kotlin.sns.domain.Review.entity.Review
import com.kotlin.sns.domain.Review.entity.ReviewSource
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * Review 레포지토리
 *
 * JPA 기본 CRUD와 QueryDSL 커스텀 쿼리를 함께 제공한다.
 */
@Repository
interface ReviewRepository : JpaRepository<Review, Long>, ReviewRepositoryCustom {

    /**
     * Member + Content 조합으로 Review 조회 (MANUAL 리뷰 중복 방지용)
     *
     * @SQLRestriction에 의해 삭제된 리뷰는 자동 제외되므로 중복 방지 동작은 유지된다.
     *
     * @param memberId 회원 ID
     * @param contentId 콘텐츠 ID
     * @return 해당 Review, 없으면 null
     */
    @Query("SELECT r FROM Review r WHERE r.member.id = :memberId AND r.content.id = :contentId")
    fun findActiveByMemberAndContent(@Param("memberId") memberId: Long, @Param("contentId") contentId: Long): Review?

    /**
     * Member + Content + Source 조합으로 Review 조회 (Steam 동기화 upsert용)
     *
     * @param memberId 회원 ID
     * @param contentId 콘텐츠 ID
     * @param source 데이터 출처
     * @return 해당 Review, 없으면 null
     */
    @Query("SELECT r FROM Review r WHERE r.member.id = :memberId AND r.content.id = :contentId AND r.source = :source")
    fun findActiveByMemberAndContentAndSource(
        @Param("memberId") memberId: Long,
        @Param("contentId") contentId: Long,
        @Param("source") source: ReviewSource
    ): Review?
}
