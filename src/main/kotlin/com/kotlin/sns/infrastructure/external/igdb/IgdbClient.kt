package com.kotlin.sns.infrastructure.external.igdb

import com.kotlin.sns.infrastructure.external.igdb.dto.IgdbGameDto
import com.kotlin.sns.infrastructure.external.igdb.dto.IgdbSearchResultDto

/**
 * IGDB API 클라이언트 인터페이스
 *
 * IGDB(Internet Game Database) API를 통해 게임 정보를 조회합니다.
 */
interface IgdbClient {

    /**
     * 키워드로 게임을 검색합니다.
     *
     * @param query 검색 키워드
     * @return 검색 결과 목록
     * @throws CustomException API 호출 실패 시 (IGDB_API_FAILED, IGDB_API_SERVER_ERROR)
     */
    fun searchGames(query: String): List<IgdbSearchResultDto>

    /**
     * IGDB ID로 게임 상세 정보를 조회합니다.
     *
     * @param id IGDB 게임 ID
     * @return 게임 상세 정보, 존재하지 않으면 null
     * @throws CustomException API 호출 실패 시 (IGDB_API_FAILED, IGDB_API_SERVER_ERROR)
     */
    fun getGameById(id: Long): IgdbGameDto?

    /**
     * Steam AppID로 IGDB 게임 상세 정보를 조회합니다.
     *
     * IGDB external_games 엔드포인트를 통해 Steam AppID → IGDB game ID를 매핑한 후,
     * 해당 게임의 상세 정보를 반환합니다.
     *
     * @param steamAppId Steam 게임 AppID
     * @return 게임 상세 정보, 매핑된 게임이 없으면 null
     * @throws CustomException API 호출 실패 시 (IGDB_API_FAILED, IGDB_API_SERVER_ERROR)
     */
    fun getGameBySteamAppId(steamAppId: Long): IgdbGameDto?
}
