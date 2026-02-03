package com.kotlin.sns.domain.Content.service

import com.kotlin.sns.domain.Content.dto.request.RequestCreateContentDto
import com.kotlin.sns.domain.Content.dto.request.RequestSearchContentDto
import com.kotlin.sns.domain.Content.dto.request.RequestUpdateContentDto
import com.kotlin.sns.domain.Content.dto.response.ResponseContentDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

/**
 * Content 도메인 서비스 인터페이스
 *
 * 콘텐츠(게임/만화/애니) 관련 비즈니스 로직을 정의한다.
 */
interface ContentService {

    /**
     * 콘텐츠 ID로 단건 조회
     *
     * @param contentId 콘텐츠 ID
     * @return 조회된 콘텐츠 정보
     * @throws CustomException 콘텐츠가 존재하지 않을 경우
     */
    fun findById(contentId: Long): ResponseContentDto

    /**
     * 전체 콘텐츠 목록 조회 (페이징)
     *
     * @param pageable 페이징 정보
     * @return 페이징된 콘텐츠 목록
     */
    fun findAll(pageable: Pageable): Page<ResponseContentDto>

    /**
     * 콘텐츠 검색 (제목 키워드, 타입 필터)
     *
     * @param pageable 페이징 정보
     * @param searchDto 검색 조건 DTO
     * @return 검색 결과 콘텐츠 목록
     */
    fun search(pageable: Pageable, searchDto: RequestSearchContentDto): Page<ResponseContentDto>

    /**
     * 새 콘텐츠 생성
     *
     * @param request 콘텐츠 생성 요청 DTO
     * @return 생성된 콘텐츠 정보
     */
    fun create(request: RequestCreateContentDto): ResponseContentDto

    /**
     * 콘텐츠 정보 수정
     *
     * @param request 콘텐츠 수정 요청 DTO
     * @return 수정된 콘텐츠 정보
     * @throws CustomException 콘텐츠가 존재하지 않을 경우
     */
    fun update(request: RequestUpdateContentDto): ResponseContentDto

    /**
     * 콘텐츠 삭제 (Soft Delete)
     *
     * @param contentId 삭제할 콘텐츠 ID
     * @throws CustomException 콘텐츠가 존재하지 않을 경우
     */
    fun delete(contentId: Long)
}
