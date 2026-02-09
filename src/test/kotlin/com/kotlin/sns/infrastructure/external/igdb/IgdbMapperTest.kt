package com.kotlin.sns.infrastructure.external.igdb

import com.kotlin.sns.domain.Content.entity.ContentType
import com.kotlin.sns.infrastructure.external.igdb.dto.IgdbGameDto
import com.kotlin.sns.infrastructure.external.igdb.mapper.IgdbMapper
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

/**
 * IgdbMapper 테스트
 *
 * IgdbGameDto → RequestCreateContentDto 변환을 검증합니다.
 */
@DisplayName("IgdbMapper 테스트")
class IgdbMapperTest {

    @Test
    @DisplayName("모든 필드가 있는 IGDB 게임 → RequestCreateContentDto 변환")
    fun `given full igdbGameDto when toCreateContentDto then map all fields`() {
        // Given
        val igdbGameDto = IgdbGameDto(
            id = 1942,
            name = "The Witcher 3: Wild Hunt",
            summary = "RPG set in a fantasy world.",
            firstReleaseDate = 1431993600, // 2015-05-19 UTC
            genres = listOf("Role-playing", "Adventure"),
            coverUrl = "//images.igdb.com/igdb/image/upload/t_cover_big/witcher3.jpg"
        )

        // When
        val result = IgdbMapper.toCreateContentDto(igdbGameDto)

        // Then
        assertAll(
            { assertEquals(ContentType.GAME, result.type) },
            { assertEquals("The Witcher 3: Wild Hunt", result.title) },
            { assertEquals("RPG set in a fantasy world.", result.description) },
            { assertEquals(2015, result.releaseYear) },
            { assertEquals("https://images.igdb.com/igdb/image/upload/t_cover_big/witcher3.jpg", result.thumbnailUrl) },
            { assertEquals(1942L, result.igdbId) },
            { assertNull(result.steamAppId) },
            { assertNull(result.malId) },
            { assertNull(result.anilistId) }
        )
    }

    @Test
    @DisplayName("optional 필드가 null인 IGDB 게임 변환")
    fun `given igdbGameDto with nulls when toCreateContentDto then nullable fields are null`() {
        // Given
        val igdbGameDto = IgdbGameDto(
            id = 999,
            name = "Unknown Game"
        )

        // When
        val result = IgdbMapper.toCreateContentDto(igdbGameDto)

        // Then
        assertAll(
            { assertEquals(ContentType.GAME, result.type) },
            { assertEquals("Unknown Game", result.title) },
            { assertNull(result.description) },
            { assertNull(result.releaseYear) },
            { assertNull(result.thumbnailUrl) },
            { assertEquals(999L, result.igdbId) }
        )
    }

    @Test
    @DisplayName("Unix timestamp → releaseYear 변환 정확성")
    fun `given firstReleaseDate as unix timestamp when convert then extract correct year`() {
        // Given: 2017-03-03 UTC
        val igdbGameDto = IgdbGameDto(
            id = 7346,
            name = "The Legend of Zelda: Breath of the Wild",
            firstReleaseDate = 1488499200
        )

        // When
        val result = IgdbMapper.toCreateContentDto(igdbGameDto)

        // Then
        assertEquals(2017, result.releaseYear)
    }

    @Test
    @DisplayName("coverUrl이 https:// 로 시작하면 그대로 유지")
    fun `given coverUrl with https when toCreateContentDto then keep as is`() {
        // Given
        val igdbGameDto = IgdbGameDto(
            id = 100,
            name = "Test Game",
            coverUrl = "https://images.igdb.com/cover.jpg"
        )

        // When
        val result = IgdbMapper.toCreateContentDto(igdbGameDto)

        // Then
        assertEquals("https://images.igdb.com/cover.jpg", result.thumbnailUrl)
    }
}
