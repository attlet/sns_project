package com.kotlin.sns.domain.Review.repository

import com.kotlin.sns.domain.Review.entity.Review
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Review 레포지토리
 *
 * JPA 기본 CRUD를 지원한다.
 */
@Repository
interface ReviewRepository : JpaRepository<Review, Long> {

    /**
     * ID로 삭제되지 않은 Review 조회
     *
     * @param id Review ID
     * @return 해당 Review, 없으면 null
     */
    fun findByIdAndIsDeletedFalse(id: Long): Review?

    /**
     * Member + Content 조합으로 Review 조회 (중복 방지용)
     *
     * @param memberId 회원 ID
     * @param contentId 콘텐츠 ID
     * @return 해당 Review, 없으면 null
     */
    fun findByMemberIdAndContentId(memberId: Long, contentId: Long): Review?
}
