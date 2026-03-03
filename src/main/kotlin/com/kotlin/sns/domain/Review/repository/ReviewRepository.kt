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
     * ID로 삭제되지 않은 Review 단건 조회
     *
     * @param id Review ID
     * @return 해당 Review, 없으면 null
     */
    @Query("SELECT r FROM Review r WHERE r.id = :id AND r.isDeleted = false")
    fun findActiveById(@Param("id") id: Long): Review?

    /**
     * Member + Content 조합으로 삭제되지 않은 Review 조회 (MANUAL 리뷰 중복 방지용)
     *
     * 소프트 딜리트된 리뷰는 중복으로 간주하지 않으므로 isDeleted 조건을 포함한다.
     *
     * @param memberId 회원 ID
     * @param contentId 콘텐츠 ID
     * @return 해당 Review, 없으면 null
     */
    @Query("SELECT r FROM Review r WHERE r.member.id = :memberId AND r.content.id = :contentId AND r.isDeleted = false")
    fun findActiveByMemberAndContent(@Param("memberId") memberId: Long, @Param("contentId") contentId: Long): Review?

    /**
     * Member + Content + Source 조합으로 삭제되지 않은 Review 조회 (Steam 동기화 upsert용)
     *
     * @param memberId 회원 ID
     * @param contentId 콘텐츠 ID
     * @param source 데이터 출처
     * @return 해당 Review, 없으면 null
     */
    @Query("SELECT r FROM Review r WHERE r.member.id = :memberId AND r.content.id = :contentId AND r.source = :source AND r.isDeleted = false")
    fun findActiveByMemberAndContentAndSource(
        @Param("memberId") memberId: Long,
        @Param("contentId") contentId: Long,
        @Param("source") source: ReviewSource
    ): Review?
}
