package com.kotlin.sns.domain.Content.repository

import com.kotlin.sns.domain.Content.dto.request.RequestSearchContentDto
import com.kotlin.sns.domain.Content.entity.Content
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

/**
 * Content 커스텀 레포지토리 인터페이스
 *
 * QueryDSL을 사용한 동적 쿼리 메서드를 정의한다.
 */
interface ContentRepositoryCustom {

    /**
     * 콘텐츠 검색 (동적 쿼리)
     *
     * 제목 키워드와 타입 필터를 적용하여 콘텐츠를 검색한다.
     *
     * @param pageable 페이징 정보
     * @param searchDto 검색 조건
     * @return 검색 결과 콘텐츠 목록
     */
    fun searchContent(pageable: Pageable, searchDto: RequestSearchContentDto): Page<Content>
}
