package com.kotlin.sns.domain.Recommendation.repository

import com.kotlin.sns.domain.Content.entity.ContentType
import com.kotlin.sns.domain.Recommendation.dto.response.ResponseRecommendationDto

/**
 * 추천 시스템 QueryDSL 쿼리 인터페이스
 */
interface RecommendationQueryRepository {

    /**
     * 인기도 기반 콘텐츠 조회
     *
     * 내가 평가하지 않은 콘텐츠 중 type 필터 + 점수(AVG(rating) * LOG(COUNT+1)) 순 정렬
     *
     * @param memberId 현재 사용자 ID (이미 평가한 콘텐츠 제외용)
     * @param type 콘텐츠 타입 필터 (null이면 전체)
     * @param limit 반환 최대 개수
     * @return 인기도 기반 추천 콘텐츠 목록
     */
    fun findPopularContents(memberId: Long, type: ContentType?, limit: Int): List<ResponseRecommendationDto>

    /**
     * 협업 필터링 기반 콘텐츠 조회
     *
     * 유사 사용자(공통 평가 콘텐츠 수 기준)가 고평점을 준 콘텐츠 중 내가 미평가한 것 반환
     *
     * @param memberId 현재 사용자 ID
     * @param type 콘텐츠 타입 필터 (null이면 전체)
     * @param limit 반환 최대 개수
     * @return 협업 필터링 추천 콘텐츠 목록
     */
    fun findCfContents(memberId: Long, type: ContentType?, limit: Int): List<ResponseRecommendationDto>
}
