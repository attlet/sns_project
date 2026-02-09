package com.kotlin.sns.infrastructure.external.igdb.dto

/**
 * IGDB 게임 상세 정보 DTO
 *
 * 게임의 상세 정보를 담는 DTO입니다.
 *
 * @property id IGDB 게임 ID
 * @property name 게임 이름
 * @property summary 게임 설명
 * @property firstReleaseDate 최초 출시일 (Unix timestamp)
 * @property genres 장르 목록
 * @property coverUrl 커버 이미지 URL
 */
data class IgdbGameDto(
    val id: Long,
    val name: String,
    val summary: String? = null,
    val firstReleaseDate: Long? = null,
    val genres: List<String> = emptyList(),
    val coverUrl: String? = null
)
