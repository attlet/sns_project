package com.kotlin.sns.infrastructure.external.steam

import com.kotlin.sns.infrastructure.external.steam.dto.SteamGameDto

/**
 * Steam Web API 클라이언트 인터페이스
 *
 * 사용자의 Steam 라이브러리 조회 기능을 정의한다.
 */
interface SteamClient {

    /**
     * Steam 사용자가 보유한 게임 목록을 조회한다.
     *
     * @param steamId Steam 64비트 사용자 ID
     * @return 보유 게임 목록 (플레이 시간 포함)
     * @throws CustomException Steam API 호출 실패 시 (STEAM_API_FAILED)
     */
    fun getOwnedGames(steamId: String): List<SteamGameDto>
}
