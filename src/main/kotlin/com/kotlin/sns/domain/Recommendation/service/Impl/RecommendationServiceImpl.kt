package com.kotlin.sns.domain.Recommendation.service.Impl

import com.kotlin.sns.domain.Content.entity.ContentType
import com.kotlin.sns.domain.Recommendation.dto.response.ResponseRecommendationDto
import com.kotlin.sns.domain.Recommendation.repository.RecommendationQueryRepository
import com.kotlin.sns.domain.Recommendation.service.RecommendationService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 추천 시스템 서비스 구현체
 *
 * 협업 필터링(CF) + 인기도 폴백 전략을 조합하여 추천 결과를 제공한다.
 *
 * @property recommendationQueryRepository 추천 QueryDSL 레포지토리
 */
@Service
class RecommendationServiceImpl(
    private val recommendationQueryRepository: RecommendationQueryRepository
) : RecommendationService {

    companion object {
        const val DEFAULT_LIMIT = 10
        const val MAX_LIMIT = 50
    }

    /**
     * 추천 콘텐츠 조회 (CF + 인기도 폴백 조합)
     *
     * CF 결과 수 < limit 이면 부족한 수만큼 인기도 기반으로 보완한다.
     * CF 결과 = 0 이면 전체 인기도 기반으로 대체한다.
     */
    @Transactional(readOnly = true)
    override fun getRecommendations(memberId: Long, type: ContentType?, limit: Int): List<ResponseRecommendationDto> {
        val cfResults = getCfRecommendations(memberId, type, limit)

        if (cfResults.size >= limit) return cfResults

        val cfContentIds = cfResults.map { it.contentId }.toSet()
        val remaining = limit - cfResults.size

        val popularResults = getPopularRecommendations(memberId, type, limit)
            .filter { it.contentId !in cfContentIds }
            .take(remaining)

        return cfResults + popularResults
    }

    /**
     * 인기도 기반 추천 콘텐츠 조회
     *
     * 내가 평가하지 않은 콘텐츠 중 평균 별점 * log(리뷰 수 + 1) 점수 순 반환
     */
    @Transactional(readOnly = true)
    override fun getPopularRecommendations(memberId: Long, type: ContentType?, limit: Int): List<ResponseRecommendationDto> {
        return recommendationQueryRepository.findPopularContents(memberId, type, limit)
    }

    /**
     * 협업 필터링 기반 추천 콘텐츠 조회
     *
     * 유사 사용자(공통 평가 콘텐츠 기준)가 고평점을 준 콘텐츠 중 내가 미평가한 것 반환
     */
    @Transactional(readOnly = true)
    override fun getCfRecommendations(memberId: Long, type: ContentType?, limit: Int): List<ResponseRecommendationDto> {
        return recommendationQueryRepository.findCfContents(memberId, type, limit)
    }
}
