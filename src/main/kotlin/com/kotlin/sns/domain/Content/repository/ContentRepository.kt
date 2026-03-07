package com.kotlin.sns.domain.Content.repository

import com.kotlin.sns.domain.Content.entity.Content
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Content 레포지토리
 *
 * JPA 기본 CRUD와 QueryDSL 커스텀 쿼리를 함께 지원한다.
 */
@Repository
interface ContentRepository : JpaRepository<Content, Long>, ContentRepositoryCustom {

    /**
     * IGDB ID로 콘텐츠 조회 (@SQLRestriction으로 삭제된 항목 자동 제외)
     *
     * @param igdbId IGDB 게임 ID
     * @return 해당 IGDB ID의 콘텐츠, 없으면 null
     */
    fun findByIgdbId(igdbId: Long): Content?
}
