package com.kotlin.sns.domain.PlatformActivityRecord.service

import com.kotlin.sns.domain.Review.dto.response.SteamSyncResultDto

/**
 * 외부 라이브러리 동기화 서비스 인터페이스
 *
 * Steam, MAL, AniList 등 외부 플랫폼의 라이브러리 동기화 로직을 정의한다.
 */
interface ExternalLibrarySyncService {

    /**
     * Steam 라이브러리 동기화
     *
     * Member의 Steam 보유 게임을 가져와 PlatformActivityRecord와 Review를 일괄 생성/업데이트한다.
     * - playtime > 0인 게임만 Review 생성 (실제 플레이한 게임)
     * - playtime == 0인 게임은 PlatformActivityRecord만 저장 (소유 기록)
     *
     * @param memberId 동기화 대상 회원 ID
     * @return 동기화 결과 (전체/생성/업데이트 수)
     * @throws CustomException steamId 미연결(STEAM_ID_NOT_LINKED), 회원 미존재(MEMBER_NOT_FOUND)
     */
    fun syncSteamLibrary(memberId: Long): SteamSyncResultDto
}
