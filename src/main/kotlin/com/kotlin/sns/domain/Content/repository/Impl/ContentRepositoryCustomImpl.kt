package com.kotlin.sns.domain.Content.repository.Impl

import com.kotlin.sns.domain.Content.dto.request.RequestSearchContentDto
import com.kotlin.sns.domain.Content.entity.Content
import com.kotlin.sns.domain.Content.entity.QContent
import com.kotlin.sns.domain.Content.repository.ContentRepositoryCustom
import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

/**
 * Content 커스텀 레포지토리 구현체
 *
 * QueryDSL을 사용하여 동적 검색 쿼리를 구현한다.
 *
 * @property jpaQueryFactory QueryDSL 쿼리 팩토리
 */
class ContentRepositoryCustomImpl(
    private val jpaQueryFactory: JPAQueryFactory
) : ContentRepositoryCustom {

    private val qContent = QContent.content

    /**
     * 콘텐츠 검색 (동적 쿼리)
     *
     * 삭제되지 않은 콘텐츠 중 조건에 맞는 항목을 조회한다.
     * 생성일 기준 내림차순으로 정렬된다.
     *
     * @param pageable 페이징 정보
     * @param searchDto 검색 조건
     * @return 검색 결과 콘텐츠 목록
     */
    override fun searchContent(pageable: Pageable, searchDto: RequestSearchContentDto): Page<Content> {
        val builder = buildSearchCondition(searchDto)

        val results = jpaQueryFactory
            .selectFrom(qContent)
            .where(builder)
            .orderBy(qContent.createdDt.desc())
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        val total = jpaQueryFactory
            .select(qContent.count())
            .from(qContent)
            .where(builder)
            .fetchOne() ?: 0L

        return PageImpl(results, pageable, total)
    }

    /**
     * 검색 조건 빌더 생성
     *
     * @param searchDto 검색 조건 DTO
     * @return BooleanBuilder 동적 조건
     */
    private fun buildSearchCondition(searchDto: RequestSearchContentDto): BooleanBuilder {
        val builder = BooleanBuilder()

        // 제목 키워드 검색 (대소문자 무시)
        searchDto.keyword?.let {
            builder.and(qContent.title.containsIgnoreCase(it))
        }

        // 타입 필터
        searchDto.type?.let {
            builder.and(qContent.type.eq(it))
        }

        return builder
    }
}
