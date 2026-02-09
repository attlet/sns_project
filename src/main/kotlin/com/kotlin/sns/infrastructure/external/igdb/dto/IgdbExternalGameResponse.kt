package com.kotlin.sns.infrastructure.external.igdb.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * IGDB external_games API 응답 DTO (내부용)
 *
 * Steam 등 외부 플랫폼의 ID로 IGDB 게임을 조회할 때 사용합니다.
 * category 1 = Steam
 *
 * @property id external_game 레코드 ID
 * @property game 매핑된 IGDB 게임 ID
 * @property uid 외부 플랫폼의 게임 ID (예: Steam AppID)
 * @property name 외부 플랫폼에서의 게임 이름
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class IgdbExternalGameResponse(
    val id: Long,
    val game: Long,
    val uid: String? = null,
    val name: String? = null
)
