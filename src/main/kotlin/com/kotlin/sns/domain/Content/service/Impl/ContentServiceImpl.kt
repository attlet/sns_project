package com.kotlin.sns.domain.Content.service.Impl

import com.kotlin.sns.common.exception.CustomException
import com.kotlin.sns.common.exception.ErrorCode
import com.kotlin.sns.domain.Content.dto.request.RequestCreateContentDto
import com.kotlin.sns.domain.Content.dto.request.RequestSearchContentDto
import com.kotlin.sns.domain.Content.dto.request.RequestUpdateContentDto
import com.kotlin.sns.domain.Content.dto.response.ResponseContentDto
import com.kotlin.sns.domain.Content.mapper.ContentMapper
import com.kotlin.sns.domain.Content.repository.ContentRepository
import com.kotlin.sns.domain.Content.service.ContentService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Content 서비스 구현체
 *
 * 콘텐츠(게임/만화/애니) CRUD 및 검색 기능을 제공한다.
 *
 * @property contentRepository 콘텐츠 레포지토리
 */
@Service
class ContentServiceImpl(
    private val contentRepository: ContentRepository
) : ContentService {

    /**
     * 콘텐츠 ID로 단건 조회
     *
     * @param contentId 콘텐츠 ID
     * @return 조회된 콘텐츠 정보
     * @throws CustomException 콘텐츠가 존재하지 않거나 삭제된 경우
     */
    @Transactional(readOnly = true)
    override fun findById(contentId: Long): ResponseContentDto {
        val content = contentRepository.findById(contentId)
            .filter { !it.isDeleted }
            .orElseThrow { CustomException(ErrorCode.CONTENT_NOT_FOUND) }

        return ContentMapper.toDto(content)
    }

    /**
     * 전체 콘텐츠 목록 조회 (페이징)
     *
     * 삭제되지 않은 콘텐츠만 조회한다.
     *
     * @param pageable 페이징 정보
     * @return 페이징된 콘텐츠 목록
     */
    @Transactional(readOnly = true)
    override fun findAll(pageable: Pageable): Page<ResponseContentDto> {
        return contentRepository.findByIsDeletedFalse(pageable)
            .map { ContentMapper.toDto(it) }
    }

    /**
     * 콘텐츠 검색 (제목 키워드, 타입 필터)
     *
     * @param pageable 페이징 정보
     * @param searchDto 검색 조건 DTO
     * @return 검색 결과 콘텐츠 목록
     */
    @Transactional(readOnly = true)
    override fun search(pageable: Pageable, searchDto: RequestSearchContentDto): Page<ResponseContentDto> {
        return contentRepository.searchContent(pageable, searchDto)
            .map { ContentMapper.toDto(it) }
    }

    /**
     * 새 콘텐츠 생성
     *
     * @param request 콘텐츠 생성 요청 DTO
     * @return 생성된 콘텐츠 정보
     */
    @Transactional
    override fun create(request: RequestCreateContentDto): ResponseContentDto {
        val content = ContentMapper.toEntity(request)
        val saved = contentRepository.save(content)
        return ContentMapper.toDto(saved)
    }

    /**
     * 콘텐츠 정보 수정
     *
     * null이 아닌 필드만 업데이트된다.
     *
     * @param request 콘텐츠 수정 요청 DTO
     * @return 수정된 콘텐츠 정보
     * @throws CustomException 콘텐츠가 존재하지 않거나 삭제된 경우
     */
    @Transactional
    override fun update(request: RequestUpdateContentDto): ResponseContentDto {
        val content = contentRepository.findById(request.contentId)
            .filter { !it.isDeleted }
            .orElseThrow { CustomException(ErrorCode.CONTENT_NOT_FOUND) }

        request.type?.let { content.type = it }
        request.title?.let { content.title = it }
        request.description?.let { content.description = it }
        request.releaseYear?.let { content.releaseYear = it }
        request.thumbnailUrl?.let { content.thumbnailUrl = it }
        request.steamAppId?.let { content.steamAppId = it }
        request.igdbId?.let { content.igdbId = it }
        request.malId?.let { content.malId = it }
        request.anilistId?.let { content.anilistId = it }

        return ContentMapper.toDto(content)
    }

    /**
     * 콘텐츠 삭제 (Soft Delete)
     *
     * 실제로 데이터를 삭제하지 않고 isDeleted 플래그를 true로 변경한다.
     *
     * @param contentId 삭제할 콘텐츠 ID
     * @throws CustomException 콘텐츠가 존재하지 않거나 이미 삭제된 경우
     */
    @Transactional
    override fun delete(contentId: Long) {
        val content = contentRepository.findById(contentId)
            .filter { !it.isDeleted }
            .orElseThrow { CustomException(ErrorCode.CONTENT_NOT_FOUND) }

        content.isDeleted = true
    }
}
