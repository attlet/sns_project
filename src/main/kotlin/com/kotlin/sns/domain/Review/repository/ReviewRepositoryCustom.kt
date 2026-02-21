package com.kotlin.sns.domain.Review.repository

import com.kotlin.sns.domain.Review.entity.Review
import com.kotlin.sns.domain.Review.entity.ReviewStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

/**
 * Review 커스텀 레포지토리 인터페이스
 *
 * QueryDSL을 사용하는 동적 쿼리 메서드를 정의한다.
 */
interface ReviewRepositoryCustom {

    /**
     * 특정 Member의 삭제되지 않은 Review 목록 조회 (동적 status 필터, 페이징)
     *
     * status가 null이면 전체를, 지정되면 해당 상태만 조회한다.
     *
     * @param memberId 회원 ID
     * @param status 리뷰 상태 필터 (null이면 전체 조회)
     * @param pageable 페이징 정보
     * @return 페이징된 Review 목록
     */
    fun findReviewsByMember(memberId: Long, status: ReviewStatus?, pageable: Pageable): Page<Review>

    /**
     * 특정 Content의 삭제되지 않은 Review 목록 조회 (페이징)
     *
     * member 정보를 fetchJoin으로 함께 조회하여 N+1을 방지한다.
     *
     * @param contentId 콘텐츠 ID
     * @param pageable 페이징 정보
     * @return 페이징된 Review 목록
     */
    fun findReviewsByContent(contentId: Long, pageable: Pageable): Page<Review>
}
