package com.kotlin.sns.domain.Recommendation.service

import com.kotlin.sns.domain.Content.entity.ContentType
import com.kotlin.sns.domain.Recommendation.dto.response.ResponseRecommendationDto

/**
 * 추천 시스템 서비스 인터페이스
 */
interface RecommendationService {

    /**
     * 추천 콘텐츠 조회 (CF + 인기도 폴백 조합)
     *
     * CF 결과가 limit보다 부족하면 인기도 기반으로 보완한다.
     * CF 결과가 0이면 전체 인기도 기반으로 대체한다.
     *
     * @param memberId 현재 사용자 ID
     * @param type 콘텐츠 타입 필터 (null이면 전체)
     * @param limit 반환 최대 개수
     * @return 추천 콘텐츠 목록
     */
    fun getRecommendations(memberId: Long, type: ContentType?, limit: Int): List<ResponseRecommendationDto>

    /**
     * 인기도 기반 추천 콘텐츠 조회
     *
     * @param memberId 현재 사용자 ID
     * @param type 콘텐츠 타입 필터 (null이면 전체)
     * @param limit 반환 최대 개수
     * @return 인기도 기반 추천 콘텐츠 목록
     */
    fun getPopularRecommendations(memberId: Long, type: ContentType?, limit: Int): List<ResponseRecommendationDto>

    /**
     * 협업 필터링 기반 추천 콘텐츠 조회
     *
     * @param memberId 현재 사용자 ID
     * @param type 콘텐츠 타입 필터 (null이면 전체)
     * @param limit 반환 최대 개수
     * @return 협업 필터링 추천 콘텐츠 목록
     */
    fun getCfRecommendations(memberId: Long, type: ContentType?, limit: Int): List<ResponseRecommendationDto>
}
