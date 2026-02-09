package com.kotlin.sns.infrastructure.external.igdb.mapper

import com.kotlin.sns.domain.Content.dto.request.RequestCreateContentDto
import com.kotlin.sns.domain.Content.entity.ContentType
import com.kotlin.sns.infrastructure.external.igdb.dto.IgdbGameDto
import java.time.Instant
import java.time.ZoneOffset

/**
 * IGDB 데이터 매퍼
 *
 * IgdbGameDto를 Content 도메인의 DTO로 변환합니다.
 */
object IgdbMapper {

    /**
     * IgdbGameDto → RequestCreateContentDto 변환
     *
     * @param dto IGDB 게임 상세 정보
     * @return Content 생성 요청 DTO (type=GAME)
     */
    fun toCreateContentDto(dto: IgdbGameDto): RequestCreateContentDto {
        return RequestCreateContentDto(
            type = ContentType.GAME,
            title = dto.name,
            description = dto.summary,
            releaseYear = dto.firstReleaseDate?.let { extractYear(it) },
            thumbnailUrl = dto.coverUrl?.let { normalizeImageUrl(it) },
            igdbId = dto.id
        )
    }

    /**
     * Unix timestamp에서 연도를 추출합니다.
     *
     * @param unixTimestamp Unix timestamp (초 단위)
     * @return 연도 (예: 2015)
     */
    private fun extractYear(unixTimestamp: Long): Int {
        return Instant.ofEpochSecond(unixTimestamp)
            .atZone(ZoneOffset.UTC)
            .year
    }

    /**
     * IGDB 이미지 URL을 정규화합니다.
     *
     * IGDB는 프로토콜 없는 URL(`//images.igdb.com/...`)을 반환하므로
     * `https:` 접두사를 추가합니다.
     *
     * @param url 원본 이미지 URL
     * @return https:// 로 시작하는 정규화된 URL
     */
    private fun normalizeImageUrl(url: String): String {
        if (url.startsWith("//")) {
            return "https:$url"
        }
        return url
    }
}
