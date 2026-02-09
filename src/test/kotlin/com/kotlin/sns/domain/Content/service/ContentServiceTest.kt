package com.kotlin.sns.domain.Content.service

import com.kotlin.sns.common.exception.CustomException
import com.kotlin.sns.common.exception.ErrorCode
import com.kotlin.sns.domain.Content.dto.request.RequestCreateContentDto
import com.kotlin.sns.domain.Content.dto.request.RequestSearchContentDto
import com.kotlin.sns.domain.Content.dto.request.RequestUpdateContentDto
import com.kotlin.sns.domain.Content.dto.response.ResponseContentDto
import com.kotlin.sns.domain.Content.entity.Content
import com.kotlin.sns.domain.Content.entity.ContentType
import com.kotlin.sns.domain.Content.mapper.ContentMapper
import com.kotlin.sns.domain.Content.repository.ContentRepository
import com.kotlin.sns.domain.Content.service.Impl.ContentServiceImpl
import com.kotlin.sns.infrastructure.external.igdb.IgdbClient
import com.kotlin.sns.infrastructure.external.igdb.dto.IgdbGameDto
import com.kotlin.sns.infrastructure.external.igdb.mapper.IgdbMapper
import io.mockk.*
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.util.*

/**
 * ContentService 단위 테스트
 *
 * MockK를 사용하여 ContentServiceImpl의 비즈니스 로직을 테스트한다.
 */
@ExtendWith(MockKExtension::class)
class ContentServiceTest {

    private val contentRepository: ContentRepository = mockk()
    private val igdbClient: IgdbClient = mockk()
    private lateinit var contentService: ContentService

    @BeforeEach
    fun setUp() {
        contentService = ContentServiceImpl(contentRepository, igdbClient)
        mockkObject(ContentMapper)
        mockkObject(IgdbMapper)
    }

    @Nested
    @DisplayName("findById 테스트")
    inner class FindByIdTest {

        @Test
        @DisplayName("존재하는 콘텐츠 조회 - 성공")
        fun findById_Success() {
            // Given
            val contentId = 1L
            val content: Content = mockk(relaxed = true) {
                every { isDeleted } returns false
            }
            val responseDto = ResponseContentDto(
                id = contentId,
                type = ContentType.GAME,
                title = "Elden Ring",
                description = "오픈월드 액션 RPG",
                releaseYear = 2022,
                thumbnailUrl = null,
                steamAppId = 1245620L,
                igdbId = null,
                malId = null,
                anilistId = null
            )

            every { contentRepository.findById(contentId) } returns Optional.of(content)
            every { ContentMapper.toDto(content) } returns responseDto

            // When
            val result = contentService.findById(contentId)

            // Then
            assertNotNull(result)
            assertEquals(responseDto, result)
            assertEquals("Elden Ring", result.title)
            verify(exactly = 1) { contentRepository.findById(contentId) }
        }

        @Test
        @DisplayName("존재하지 않는 콘텐츠 조회 - 실패")
        fun findById_NotFound() {
            // Given
            val contentId = 999L
            every { contentRepository.findById(contentId) } returns Optional.empty()

            // When & Then
            val exception = assertThrows(CustomException::class.java) {
                contentService.findById(contentId)
            }
            assertEquals(ErrorCode.CONTENT_NOT_FOUND, exception.errorCode)
        }

        @Test
        @DisplayName("삭제된 콘텐츠 조회 - 실패")
        fun findById_DeletedContent() {
            // Given
            val contentId = 1L
            val deletedContent: Content = mockk(relaxed = true) {
                every { isDeleted } returns true
            }
            every { contentRepository.findById(contentId) } returns Optional.of(deletedContent)

            // When & Then
            val exception = assertThrows(CustomException::class.java) {
                contentService.findById(contentId)
            }
            assertEquals(ErrorCode.CONTENT_NOT_FOUND, exception.errorCode)
        }
    }

    @Nested
    @DisplayName("findAll 테스트")
    inner class FindAllTest {

        @Test
        @DisplayName("전체 콘텐츠 목록 조회 - 성공")
        fun findAll_Success() {
            // Given
            val pageable = PageRequest.of(0, 10)
            val content1: Content = mockk(relaxed = true)
            val content2: Content = mockk(relaxed = true)
            val contentList = listOf(content1, content2)
            val page = PageImpl(contentList, pageable, 2)

            val responseDto1 = ResponseContentDto(
                id = 1L, type = ContentType.GAME, title = "Game 1",
                description = null, releaseYear = null, thumbnailUrl = null,
                steamAppId = null, igdbId = null, malId = null, anilistId = null
            )
            val responseDto2 = ResponseContentDto(
                id = 2L, type = ContentType.ANIME, title = "Anime 1",
                description = null, releaseYear = null, thumbnailUrl = null,
                steamAppId = null, igdbId = null, malId = null, anilistId = null
            )

            every { contentRepository.findByIsDeletedFalse(pageable) } returns page
            every { ContentMapper.toDto(content1) } returns responseDto1
            every { ContentMapper.toDto(content2) } returns responseDto2

            // When
            val result = contentService.findAll(pageable)

            // Then
            assertEquals(2, result.content.size)
            assertEquals(2, result.totalElements)
            verify(exactly = 1) { contentRepository.findByIsDeletedFalse(pageable) }
        }

        @Test
        @DisplayName("빈 목록 조회 - 성공")
        fun findAll_Empty() {
            // Given
            val pageable = PageRequest.of(0, 10)
            val emptyPage = PageImpl<Content>(emptyList(), pageable, 0)

            every { contentRepository.findByIsDeletedFalse(pageable) } returns emptyPage

            // When
            val result = contentService.findAll(pageable)

            // Then
            assertTrue(result.content.isEmpty())
            assertEquals(0, result.totalElements)
        }
    }

    @Nested
    @DisplayName("search 테스트")
    inner class SearchTest {

        @Test
        @DisplayName("키워드 검색 - 성공")
        fun search_WithKeyword_Success() {
            // Given
            val pageable = PageRequest.of(0, 10)
            val searchDto = RequestSearchContentDto(keyword = "Ring")
            val content: Content = mockk(relaxed = true)
            val page = PageImpl(listOf(content), pageable, 1)

            val responseDto = ResponseContentDto(
                id = 1L, type = ContentType.GAME, title = "Elden Ring",
                description = null, releaseYear = 2022, thumbnailUrl = null,
                steamAppId = null, igdbId = null, malId = null, anilistId = null
            )

            every { contentRepository.searchContent(pageable, searchDto) } returns page
            every { ContentMapper.toDto(content) } returns responseDto

            // When
            val result = contentService.search(pageable, searchDto)

            // Then
            assertEquals(1, result.content.size)
            assertEquals("Elden Ring", result.content[0].title)
            verify(exactly = 1) { contentRepository.searchContent(pageable, searchDto) }
        }

        @Test
        @DisplayName("타입 필터 검색 - 성공")
        fun search_WithType_Success() {
            // Given
            val pageable = PageRequest.of(0, 10)
            val searchDto = RequestSearchContentDto(type = ContentType.ANIME)
            val content: Content = mockk(relaxed = true)
            val page = PageImpl(listOf(content), pageable, 1)

            val responseDto = ResponseContentDto(
                id = 1L, type = ContentType.ANIME, title = "진격의 거인",
                description = null, releaseYear = 2013, thumbnailUrl = null,
                steamAppId = null, igdbId = null, malId = 16498L, anilistId = null
            )

            every { contentRepository.searchContent(pageable, searchDto) } returns page
            every { ContentMapper.toDto(content) } returns responseDto

            // When
            val result = contentService.search(pageable, searchDto)

            // Then
            assertEquals(1, result.content.size)
            assertEquals(ContentType.ANIME, result.content[0].type)
        }
    }

    @Nested
    @DisplayName("create 테스트")
    inner class CreateTest {

        @Test
        @DisplayName("게임 콘텐츠 생성 - 성공")
        fun create_GameContent_Success() {
            // Given
            val request = RequestCreateContentDto(
                type = ContentType.GAME,
                title = "Elden Ring",
                description = "오픈월드 액션 RPG",
                releaseYear = 2022,
                steamAppId = 1245620L
            )
            val content: Content = mockk(relaxed = true)
            val savedContent: Content = mockk(relaxed = true)
            val responseDto = ResponseContentDto(
                id = 1L, type = ContentType.GAME, title = "Elden Ring",
                description = "오픈월드 액션 RPG", releaseYear = 2022, thumbnailUrl = null,
                steamAppId = 1245620L, igdbId = null, malId = null, anilistId = null
            )

            every { ContentMapper.toEntity(request) } returns content
            every { contentRepository.save(content) } returns savedContent
            every { ContentMapper.toDto(savedContent) } returns responseDto

            // When
            val result = contentService.create(request)

            // Then
            assertNotNull(result)
            assertEquals("Elden Ring", result.title)
            assertEquals(ContentType.GAME, result.type)
            assertEquals(1245620L, result.steamAppId)
            verify(exactly = 1) { contentRepository.save(content) }
        }

        @Test
        @DisplayName("애니메이션 콘텐츠 생성 - 성공")
        fun create_AnimeContent_Success() {
            // Given
            val request = RequestCreateContentDto(
                type = ContentType.ANIME,
                title = "진격의 거인",
                releaseYear = 2013,
                malId = 16498L
            )
            val content: Content = mockk(relaxed = true)
            val savedContent: Content = mockk(relaxed = true)
            val responseDto = ResponseContentDto(
                id = 1L, type = ContentType.ANIME, title = "진격의 거인",
                description = null, releaseYear = 2013, thumbnailUrl = null,
                steamAppId = null, igdbId = null, malId = 16498L, anilistId = null
            )

            every { ContentMapper.toEntity(request) } returns content
            every { contentRepository.save(content) } returns savedContent
            every { ContentMapper.toDto(savedContent) } returns responseDto

            // When
            val result = contentService.create(request)

            // Then
            assertNotNull(result)
            assertEquals(ContentType.ANIME, result.type)
            assertEquals(16498L, result.malId)
        }
    }

    @Nested
    @DisplayName("update 테스트")
    inner class UpdateTest {

        @Test
        @DisplayName("콘텐츠 제목 수정 - 성공")
        fun update_Title_Success() {
            // Given
            val contentId = 1L
            val request = RequestUpdateContentDto(
                contentId = contentId,
                title = "Updated Title"
            )
            val content: Content = mockk(relaxed = true) {
                every { isDeleted } returns false
            }
            val responseDto = ResponseContentDto(
                id = contentId, type = ContentType.GAME, title = "Updated Title",
                description = null, releaseYear = null, thumbnailUrl = null,
                steamAppId = null, igdbId = null, malId = null, anilistId = null
            )

            every { contentRepository.findById(contentId) } returns Optional.of(content)
            every { ContentMapper.toDto(content) } returns responseDto

            // When
            val result = contentService.update(request)

            // Then
            assertNotNull(result)
            assertEquals("Updated Title", result.title)
            verify { content.title = "Updated Title" }
            verify(exactly = 1) { contentRepository.findById(contentId) }
        }

        @Test
        @DisplayName("콘텐츠 여러 필드 수정 - 성공")
        fun update_MultipleFields_Success() {
            // Given
            val contentId = 1L
            val request = RequestUpdateContentDto(
                contentId = contentId,
                title = "New Title",
                description = "New Description",
                releaseYear = 2024
            )
            val content: Content = mockk(relaxed = true) {
                every { isDeleted } returns false
            }
            val responseDto = ResponseContentDto(
                id = contentId, type = ContentType.GAME, title = "New Title",
                description = "New Description", releaseYear = 2024, thumbnailUrl = null,
                steamAppId = null, igdbId = null, malId = null, anilistId = null
            )

            every { contentRepository.findById(contentId) } returns Optional.of(content)
            every { ContentMapper.toDto(content) } returns responseDto

            // When
            contentService.update(request)

            // Then
            verify { content.title = "New Title" }
            verify { content.description = "New Description" }
            verify { content.releaseYear = 2024 }
        }

        @Test
        @DisplayName("존재하지 않는 콘텐츠 수정 - 실패")
        fun update_NotFound() {
            // Given
            val request = RequestUpdateContentDto(contentId = 999L, title = "New Title")
            every { contentRepository.findById(999L) } returns Optional.empty()

            // When & Then
            val exception = assertThrows(CustomException::class.java) {
                contentService.update(request)
            }
            assertEquals(ErrorCode.CONTENT_NOT_FOUND, exception.errorCode)
        }

        @Test
        @DisplayName("삭제된 콘텐츠 수정 - 실패")
        fun update_DeletedContent() {
            // Given
            val contentId = 1L
            val request = RequestUpdateContentDto(contentId = contentId, title = "New Title")
            val deletedContent: Content = mockk(relaxed = true) {
                every { isDeleted } returns true
            }
            every { contentRepository.findById(contentId) } returns Optional.of(deletedContent)

            // When & Then
            val exception = assertThrows(CustomException::class.java) {
                contentService.update(request)
            }
            assertEquals(ErrorCode.CONTENT_NOT_FOUND, exception.errorCode)
        }
    }

    @Nested
    @DisplayName("delete 테스트")
    inner class DeleteTest {

        @Test
        @DisplayName("콘텐츠 삭제 (Soft Delete) - 성공")
        fun delete_Success() {
            // Given
            val contentId = 1L
            val content: Content = mockk(relaxed = true) {
                every { isDeleted } returns false
            }
            every { contentRepository.findById(contentId) } returns Optional.of(content)

            // When
            contentService.delete(contentId)

            // Then
            verify { content.isDeleted = true }
            verify(exactly = 1) { contentRepository.findById(contentId) }
        }

        @Test
        @DisplayName("존재하지 않는 콘텐츠 삭제 - 실패")
        fun delete_NotFound() {
            // Given
            val contentId = 999L
            every { contentRepository.findById(contentId) } returns Optional.empty()

            // When & Then
            val exception = assertThrows(CustomException::class.java) {
                contentService.delete(contentId)
            }
            assertEquals(ErrorCode.CONTENT_NOT_FOUND, exception.errorCode)
        }

        @Test
        @DisplayName("이미 삭제된 콘텐츠 삭제 - 실패")
        fun delete_AlreadyDeleted() {
            // Given
            val contentId = 1L
            val deletedContent: Content = mockk(relaxed = true) {
                every { isDeleted } returns true
            }
            every { contentRepository.findById(contentId) } returns Optional.of(deletedContent)

            // When & Then
            val exception = assertThrows(CustomException::class.java) {
                contentService.delete(contentId)
            }
            assertEquals(ErrorCode.CONTENT_NOT_FOUND, exception.errorCode)
        }
    }

    @Nested
    @DisplayName("importGameFromIgdb 테스트")
    inner class ImportGameFromIgdbTest {

        @Test
        @DisplayName("IGDB 게임 임포트 - 성공")
        fun importGameFromIgdb_Success() {
            // Given
            val igdbId = 1942L
            val igdbGameDto = IgdbGameDto(
                id = igdbId,
                name = "The Witcher 3: Wild Hunt",
                summary = "RPG set in a fantasy world.",
                firstReleaseDate = 1431993600,
                genres = listOf("Role-playing"),
                coverUrl = "//images.igdb.com/witcher3.jpg"
            )
            val createDto = RequestCreateContentDto(
                type = ContentType.GAME,
                title = "The Witcher 3: Wild Hunt",
                description = "RPG set in a fantasy world.",
                releaseYear = 2015,
                thumbnailUrl = "https://images.igdb.com/witcher3.jpg",
                igdbId = igdbId
            )
            val content: Content = mockk(relaxed = true)
            val savedContent: Content = mockk(relaxed = true)
            val responseDto = ResponseContentDto(
                id = 1L, type = ContentType.GAME, title = "The Witcher 3: Wild Hunt",
                description = "RPG set in a fantasy world.", releaseYear = 2015,
                thumbnailUrl = "https://images.igdb.com/witcher3.jpg",
                steamAppId = null, igdbId = igdbId, malId = null, anilistId = null
            )

            every { contentRepository.findByIgdbIdAndIsDeletedFalse(igdbId) } returns null
            every { igdbClient.getGameById(igdbId) } returns igdbGameDto
            every { IgdbMapper.toCreateContentDto(igdbGameDto) } returns createDto
            every { ContentMapper.toEntity(createDto) } returns content
            every { contentRepository.save(content) } returns savedContent
            every { ContentMapper.toDto(savedContent) } returns responseDto

            // When
            val result = contentService.importGameFromIgdb(igdbId)

            // Then
            assertAll(
                { assertEquals("The Witcher 3: Wild Hunt", result.title) },
                { assertEquals(ContentType.GAME, result.type) },
                { assertEquals(igdbId, result.igdbId) },
                { assertEquals(2015, result.releaseYear) }
            )
            verify(exactly = 1) { igdbClient.getGameById(igdbId) }
            verify(exactly = 1) { contentRepository.save(content) }
        }

        @Test
        @DisplayName("존재하지 않는 IGDB ID로 임포트 - 실패")
        fun importGameFromIgdb_NotFound() {
            // Given
            val igdbId = 999999L
            every { contentRepository.findByIgdbIdAndIsDeletedFalse(igdbId) } returns null
            every { igdbClient.getGameById(igdbId) } returns null

            // When & Then
            val exception = assertThrows(CustomException::class.java) {
                contentService.importGameFromIgdb(igdbId)
            }
            assertEquals(ErrorCode.IGDB_GAME_NOT_FOUND, exception.errorCode)
            verify(exactly = 1) { igdbClient.getGameById(igdbId) }
            verify(exactly = 0) { contentRepository.save(any()) }
        }

        @Test
        @DisplayName("이미 존재하는 igdbId로 임포트 시 기존 Content 반환")
        fun importGameFromIgdb_Duplicate() {
            // Given
            val igdbId = 1942L
            val existingContent: Content = mockk(relaxed = true)
            val responseDto = ResponseContentDto(
                id = 1L, type = ContentType.GAME, title = "The Witcher 3: Wild Hunt",
                description = "RPG set in a fantasy world.", releaseYear = 2015,
                thumbnailUrl = "https://images.igdb.com/witcher3.jpg",
                steamAppId = null, igdbId = igdbId, malId = null, anilistId = null
            )

            every { contentRepository.findByIgdbIdAndIsDeletedFalse(igdbId) } returns existingContent
            every { ContentMapper.toDto(existingContent) } returns responseDto

            // When
            val result = contentService.importGameFromIgdb(igdbId)

            // Then
            assertAll(
                { assertEquals("The Witcher 3: Wild Hunt", result.title) },
                { assertEquals(igdbId, result.igdbId) }
            )
            verify(exactly = 0) { igdbClient.getGameById(any()) }
            verify(exactly = 0) { contentRepository.save(any()) }
        }
    }
}
