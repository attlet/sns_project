package com.kotlin.sns.domain.Content.controller

import com.kotlin.sns.domain.Content.dto.request.RequestCreateContentDto
import com.kotlin.sns.domain.Content.dto.request.RequestSearchContentDto
import com.kotlin.sns.domain.Content.dto.request.RequestUpdateContentDto
import com.kotlin.sns.domain.Content.dto.response.ResponseContentDto
import com.kotlin.sns.domain.Content.entity.ContentType
import com.kotlin.sns.domain.Content.service.ContentService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

/**
 * Content 컨트롤러
 *
 * 콘텐츠(게임/만화/애니) 관련 REST API를 제공한다.
 *
 * @property contentService 콘텐츠 서비스
 */
@RestController
@RequestMapping("/contents")
@Tag(name = "Content", description = "콘텐츠(게임/만화/애니) 관련 API")
class ContentController(
    private val contentService: ContentService
) {

    /**
     * 콘텐츠 단건 조회
     *
     * @param contentId 조회할 콘텐츠 ID
     * @return 조회된 콘텐츠 정보
     */
    @GetMapping("/{contentId}")
    @Operation(summary = "콘텐츠 단건 조회")
    fun getContent(@PathVariable contentId: Long): ResponseContentDto {
        return contentService.findById(contentId)
    }

    /**
     * 콘텐츠 목록 조회 (페이징)
     *
     * @param page 페이지 번호 (0부터 시작, 기본값: 0)
     * @param size 페이지 크기 (기본값: 10)
     * @return 페이징된 콘텐츠 목록
     */
    @GetMapping
    @Operation(summary = "콘텐츠 목록 조회 (페이징)")
    fun getContentList(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): Page<ResponseContentDto> {
        return contentService.findAll(PageRequest.of(page, size))
    }

    /**
     * 콘텐츠 검색 (제목 키워드, 타입 필터)
     *
     * @param page 페이지 번호 (0부터 시작, 기본값: 0)
     * @param size 페이지 크기 (기본값: 10)
     * @param keyword 제목 검색 키워드 (선택)
     * @param type 콘텐츠 타입 필터 (선택)
     * @return 검색 결과 콘텐츠 목록
     */
    @GetMapping("/search")
    @Operation(summary = "콘텐츠 검색 (제목, 타입 필터)")
    fun searchContent(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false) type: ContentType?
    ): Page<ResponseContentDto> {
        val searchDto = RequestSearchContentDto(keyword = keyword, type = type)
        return contentService.search(PageRequest.of(page, size), searchDto)
    }

    /**
     * 새 콘텐츠 생성
     *
     * @param request 콘텐츠 생성 요청 DTO
     * @return 생성된 콘텐츠 정보
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "콘텐츠 생성")
    fun createContent(@RequestBody request: RequestCreateContentDto): ResponseContentDto {
        return contentService.create(request)
    }

    /**
     * 콘텐츠 정보 수정
     *
     * @param request 콘텐츠 수정 요청 DTO
     * @return 수정된 콘텐츠 정보
     */
    @PutMapping
    @Operation(summary = "콘텐츠 수정")
    fun updateContent(@RequestBody request: RequestUpdateContentDto): ResponseContentDto {
        return contentService.update(request)
    }

    /**
     * 콘텐츠 삭제 (Soft Delete)
     *
     * @param contentId 삭제할 콘텐츠 ID
     */
    @DeleteMapping("/{contentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "콘텐츠 삭제 (Soft Delete)")
    fun deleteContent(@PathVariable contentId: Long) {
        contentService.delete(contentId)
    }
}
