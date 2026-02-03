package com.kotlin.sns.domain.Content.repository

import com.kotlin.sns.domain.Content.entity.Content
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
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
     * 삭제되지 않은 콘텐츠 목록 조회 (페이징)
     *
     * @param pageable 페이징 정보
     * @return 페이징된 콘텐츠 목록
     */
    fun findByIsDeletedFalse(pageable: Pageable): Page<Content>
}
