package com.kotlin.sns.infrastructure.external.igdb.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * IGDB 게임 검색 결과 DTO
 *
 * @property id IGDB 게임 ID
 * @property name 게임 이름
 * @property coverUrl 커버 이미지 URL
 */
data class IgdbSearchResultDto(
    val id: Long,
    val name: String,

    @JsonProperty("cover_url")
    val coverUrl: String? = null
)
