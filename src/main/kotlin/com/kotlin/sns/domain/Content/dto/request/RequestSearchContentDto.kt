package com.kotlin.sns.domain.Content.dto.request

import com.kotlin.sns.domain.Content.entity.ContentType

/**
 * 콘텐츠 검색 요청 DTO
 *
 * @property keyword 제목 검색 키워드 (대소문자 무시)
 * @property type 콘텐츠 타입 필터
 */
data class RequestSearchContentDto(
    val keyword: String? = null,
    val type: ContentType? = null
)
