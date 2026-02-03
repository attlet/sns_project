package com.kotlin.sns.domain.Content.mapper

import com.kotlin.sns.domain.Content.dto.request.RequestCreateContentDto
import com.kotlin.sns.domain.Content.dto.response.ResponseContentDto
import com.kotlin.sns.domain.Content.entity.Content

/**
 * Content 매퍼
 *
 * DTO와 Entity 간의 변환을 담당한다.
 */
object ContentMapper {

    /**
     * 생성 요청 DTO를 Entity로 변환
     *
     * @param dto 콘텐츠 생성 요청 DTO
     * @return Content 엔티티
     */
    fun toEntity(dto: RequestCreateContentDto): Content {
        return Content(
            type = dto.type,
            title = dto.title,
            description = dto.description,
            releaseYear = dto.releaseYear,
            thumbnailUrl = dto.thumbnailUrl,
            steamAppId = dto.steamAppId,
            igdbId = dto.igdbId,
            malId = dto.malId,
            anilistId = dto.anilistId
        )
    }

    /**
     * Entity를 응답 DTO로 변환
     *
     * @param content Content 엔티티
     * @return 콘텐츠 응답 DTO
     */
    fun toDto(content: Content): ResponseContentDto {
        return ResponseContentDto(
            id = content.id,
            type = content.type,
            title = content.title,
            description = content.description,
            releaseYear = content.releaseYear,
            thumbnailUrl = content.thumbnailUrl,
            steamAppId = content.steamAppId,
            igdbId = content.igdbId,
            malId = content.malId,
            anilistId = content.anilistId
        )
    }
}
