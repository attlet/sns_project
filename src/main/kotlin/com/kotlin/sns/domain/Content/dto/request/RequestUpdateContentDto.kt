package com.kotlin.sns.domain.Content.dto.request

import com.kotlin.sns.domain.Content.entity.ContentType

/**
 * 콘텐츠 수정 요청 DTO
 *
 * null이 아닌 필드만 업데이트된다.
 *
 * @property contentId 수정할 콘텐츠 ID (필수)
 * @property type 콘텐츠 타입
 * @property title 제목
 * @property description 설명
 * @property releaseYear 출시년도
 * @property thumbnailUrl 썸네일 이미지 URL
 * @property steamAppId Steam 앱 ID
 * @property igdbId IGDB ID
 * @property malId MyAnimeList ID
 * @property anilistId AniList ID
 */
data class RequestUpdateContentDto(
    val contentId: Long,
    val type: ContentType? = null,
    val title: String? = null,
    val description: String? = null,
    val releaseYear: Int? = null,
    val thumbnailUrl: String? = null,
    val steamAppId: Long? = null,
    val igdbId: Long? = null,
    val malId: Long? = null,
    val anilistId: Long? = null
)
