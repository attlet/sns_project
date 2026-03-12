package com.kotlin.sns.infrastructure.external.steam.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Steam Store API 응답 매핑 DTO
 *
 * `GET https://store.steampowered.com/api/appdetails?appids={appId}` 응답을 파싱한다.
 * 인증 불필요. 최상위 key가 appId(문자열)인 특수 구조를 `Map<String, AppEntry>`로 역직렬화한다.
 *
 * ## 응답 예시
 * ```json
 * {
 *   "570": {
 *     "success": true,
 *     "data": {
 *       "name": "Dota 2",
 *       "short_description": "게임 설명",
 *       "header_image": "https://cdn.akamai.steamstatic.com/steam/apps/570/header.jpg",
 *       "genres": [
 *         { "id": "1", "description": "Action" },
 *         { "id": "25", "description": "Adventure" }
 *       ],
 *       "release_date": { "date": "Jul 9, 2013" }
 *     }
 *   }
 * }
 * ```
 *
 * @property name 게임 제목
 * @property shortDescription 짧은 게임 설명
 * @property headerImage 헤더 이미지 URL
 * @property genres 장르 목록 (예: ["Action", "Adventure"])
 * @property releaseYear 출시 연도 (release_date.date 에서 파싱, 실패 시 null)
 */
data class SteamAppDetailsDto(
    val name: String,
    val shortDescription: String?,
    val headerImage: String?,
    val genres: List<String>,
    val releaseYear: Int?
) {
    companion object {
        /**
         * Store API 원본 응답 Map에서 DTO를 생성한다.
         *
         * @param appId Steam 게임 App ID
         * @param response Store API 응답 (`Map<String, AppEntry>` 형태)
         * @return 파싱된 DTO, appId 없거나 success=false이면 null
         */
        fun from(appId: Int, response: Map<String, AppEntry>): SteamAppDetailsDto? {
            val entry = response[appId.toString()] ?: return null
            if (!entry.success || entry.data == null) return null
            val data = entry.data
            return SteamAppDetailsDto(
                name = data.name,
                shortDescription = data.shortDescription,
                headerImage = data.headerImage,
                genres = data.genres.map { it.description },
                releaseYear = data.releaseDate?.date?.takeLast(4)?.toIntOrNull()
            )
        }
    }

    /**
     * 최상위 Map의 value. appId 키에 매핑되는 응답 단위.
     *
     * @property success API 조회 성공 여부
     * @property data 게임 상세 데이터 (success=false 시 null)
     */
    data class AppEntry(
        val success: Boolean,
        val data: AppData?
    )

    /**
     * 게임 상세 데이터.
     *
     * @property name 게임 제목
     * @property shortDescription 짧은 설명
     * @property headerImage 헤더 이미지 URL
     * @property genres 장르 목록
     * @property releaseDate 출시일 정보
     */
    data class AppData(
        val name: String,
        @JsonProperty("short_description") val shortDescription: String?,
        @JsonProperty("header_image") val headerImage: String?,
        val genres: List<GenreEntry> = emptyList(),
        @JsonProperty("release_date") val releaseDate: ReleaseDate?
    )

    /**
     * 장르 항목.
     *
     * @property id Steam 장르 ID
     * @property description 장르명 (예: "Action")
     */
    data class GenreEntry(
        val id: String,
        val description: String
    )

    /**
     * 출시일 정보.
     *
     * @property date 출시일 문자열 (예: "Jul 9, 2013"). 포맷이 비일관적이므로 연도만 파싱.
     */
    data class ReleaseDate(
        val date: String?
    )
}
