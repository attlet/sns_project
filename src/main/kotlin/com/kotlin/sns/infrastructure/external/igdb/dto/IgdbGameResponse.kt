package com.kotlin.sns.infrastructure.external.igdb.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * IGDB API 게임 응답 DTO (내부용)
 *
 * IGDB API의 실제 응답 구조를 매핑합니다.
 * cover, genres 필드가 객체 형태로 오기 때문에 별도 클래스로 분리합니다.
 *
 * @property id IGDB 게임 ID
 * @property name 게임 이름
 * @property summary 게임 설명
 * @property firstReleaseDate 최초 출시일 (Unix timestamp)
 * @property cover 커버 이미지 정보 (nullable)
 * @property genres 장르 목록
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class IgdbGameResponse(
    val id: Long,
    val name: String,
    val summary: String? = null,
    @JsonProperty("first_release_date")
    val firstReleaseDate: Long? = null,
    val cover: IgdbCover? = null,
    val genres: List<IgdbGenre>? = null
) {
    /**
     * IgdbSearchResultDto로 변환합니다.
     */
    fun toSearchResult(): IgdbSearchResultDto = IgdbSearchResultDto(
        id = id,
        name = name,
        coverUrl = cover?.url
    )

    /**
     * IgdbGameDto로 변환합니다.
     */
    fun toGameDto(): IgdbGameDto = IgdbGameDto(
        id = id,
        name = name,
        summary = summary,
        firstReleaseDate = firstReleaseDate,
        genres = genres?.mapNotNull { it.name } ?: emptyList(),
        coverUrl = cover?.url
    )
}

/**
 * IGDB 커버 이미지 정보
 *
 * @property url 이미지 URL
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class IgdbCover(
    val url: String? = null
)

/**
 * IGDB 장르 정보
 *
 * @property id 장르 ID
 * @property name 장르 이름
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class IgdbGenre(
    val id: Long? = null,
    val name: String? = null
)
