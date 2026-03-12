package com.kotlin.sns.domain.PlatformActivityRecord.repository

import com.kotlin.sns.domain.PlatformActivityRecord.entity.PlatformActivityRecord
import com.kotlin.sns.domain.Review.entity.ReviewSource
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * PlatformActivityRecord 레포지토리
 *
 * 외부 플랫폼 활동 기록에 대한 JPA CRUD 쿼리를 제공한다.
 */
@Repository
interface PlatformActivityRecordRepository : JpaRepository<PlatformActivityRecord, Long> {

    /**
     * Member + Content + Source 조합으로 삭제되지 않은 레코드 조회 (동기화 중복 방지용)
     *
     * @param memberId 회원 ID
     * @param contentId 콘텐츠 ID
     * @param source 데이터 출처 (STEAM, MAL, ANILIST)
     * @return 해당 레코드, 없으면 null
     */
    @Query(
        "SELECT r FROM PlatformActivityRecord r " +
        "WHERE r.member.id = :memberId AND r.content.id = :contentId " +
        "AND r.source = :source"
    )
    fun findActiveByMemberAndContentAndSource(
        @Param("memberId") memberId: Long,
        @Param("contentId") contentId: Long,
        @Param("source") source: ReviewSource
    ): PlatformActivityRecord?
}
