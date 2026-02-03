package com.kotlin.sns.domain.Content.dto.request

import com.kotlin.sns.domain.Content.entity.ContentType

/**
 * 콘텐츠 생성 요청 DTO
 *
 * @property type 콘텐츠 타입 (GAME, MANGA, ANIME)
 * @property title 제목 (필수)
 * @property description 설명
 * @property releaseYear 출시년도
 * @property thumbnailUrl 썸네일 이미지 URL
 * @property steamAppId Steam 앱 ID
 * @property igdbId IGDB ID
 * @property malId MyAnimeList ID
 * @property anilistId AniList ID
 */
data class RequestCreateContentDto(
    val type: ContentType,
    val title: String,
    val description: String? = null,
    val releaseYear: Int? = null,
    val thumbnailUrl: String? = null,
    val steamAppId: Long? = null,
    val igdbId: Long? = null,
    val malId: Long? = null,
    val anilistId: Long? = null
)
