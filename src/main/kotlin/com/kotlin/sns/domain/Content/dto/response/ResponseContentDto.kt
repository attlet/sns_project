package com.kotlin.sns.domain.Content.dto.response

import com.kotlin.sns.domain.Content.entity.ContentType

/**
 * 콘텐츠 응답 DTO
 *
 * @property id 콘텐츠 ID
 * @property type 콘텐츠 타입 (GAME, MANGA, ANIME)
 * @property title 제목
 * @property description 설명
 * @property releaseYear 출시년도
 * @property thumbnailUrl 썸네일 이미지 URL
 * @property steamAppId Steam 앱 ID
 * @property igdbId IGDB ID
 * @property malId MyAnimeList ID
 * @property anilistId AniList ID
 */
data class ResponseContentDto(
    val id: Long,
    val type: ContentType,
    val title: String,
    val description: String?,
    val releaseYear: Int?,
    val thumbnailUrl: String?,
    val steamAppId: Long?,
    val igdbId: Long?,
    val malId: Long?,
    val anilistId: Long?
)
