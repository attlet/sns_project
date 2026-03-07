package com.kotlin.sns.domain.Recommendation.repository.Impl

import com.kotlin.sns.domain.Content.entity.ContentType
import com.kotlin.sns.domain.Content.entity.QContent
import com.kotlin.sns.domain.Recommendation.config.RecommendationProperties
import com.kotlin.sns.domain.Recommendation.dto.response.RecommendationType
import com.kotlin.sns.domain.Recommendation.dto.response.ResponseRecommendationDto
import com.kotlin.sns.domain.Recommendation.repository.RecommendationQueryRepository
import com.kotlin.sns.domain.Review.entity.QReview
import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

/**
 * 추천 시스템 QueryDSL 구현체
 *
 * @property jpaQueryFactory QueryDSL 쿼리 팩토리
 * @property props 추천 알고리즘 튜닝 파라미터 (application.yml `recommendation.cf`)
 *
 * props 구조
 *
 * similarUserK 유사 사용자 최대 추출 수 (상위 K명)
 * similarUserMinCoCount 유사 사용자로 인정하기 위한 최소 공통 콘텐츠 수
 * minRating 유사도 계산에 포함할 최소 평점
 * recommendRating 추천 기준 최소 평점 (유사 사용자가 이 점수 이상 준 콘텐츠만 추천)
 */
@Repository
class RecommendationQueryRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory,
    private val props: RecommendationProperties
) : RecommendationQueryRepository {

    private val qContent = QContent.content
    private val qReview = QReview.review

    /**
     * 인기도 기반 콘텐츠 조회
     *
     * 내가 평가하지 않은 콘텐츠 중 AVG(rating) DESC, COUNT DESC 순으로 반환한다.
     */
    override fun findPopularContents(memberId: Long, type: ContentType?, limit: Int): List<ResponseRecommendationDto> {
        val myContentIds = fetchMyRatedContentIds(memberId)
        val avgRating = qReview.rating.avg()
        val reviewCount = qReview.count()

        return jpaQueryFactory
            .select(qContent.id, qContent.title, qContent.type, qContent.thumbnailUrl, avgRating, reviewCount)
            .from(qContent)
            .leftJoin(qReview).on(
                qReview.content.id.eq(qContent.id),
                qReview.rating.isNotNull
            )
            .where(buildPopularConditions(myContentIds, type))
            .groupBy(qContent.id)
            .having(reviewCount.gt(0))
            .orderBy(avgRating.desc(), reviewCount.desc())
            .limit(limit.toLong())
            .fetch()
            .map {
                ResponseRecommendationDto(
                    contentId = it.get(qContent.id)!!,
                    title = it.get(qContent.title)!!,
                    type = it.get(qContent.type)!!,
                    thumbnailUrl = it.get(qContent.thumbnailUrl),
                    avgRating = it.get(avgRating) ?: 0.0,
                    reviewCount = it.get(reviewCount)?.toInt() ?: 0,
                    recommendationType = RecommendationType.POPULAR
                )
            }
    }

    /**
     * 협업 필터링 기반 콘텐츠 조회
     *
     * Step 1. 내 평가 콘텐츠 ID 목록 (rating >= CF_MIN_RATING)
     * Step 2. 유사 사용자 조회 (공통 콘텐츠 수 >= SIMILAR_USER_MIN_CO_COUNT 인 상위 K명)
     * Step 3. 유사 사용자들의 고평점(rating >= CF_RECOMMEND_RATING) 콘텐츠 중 내 미평가 반환
     */
    override fun findCfContents(memberId: Long, type: ContentType?, limit: Int): List<ResponseRecommendationDto> {
        // 1. 내가 rating >= minRating 으로 평가한 콘텐츠 ID 수집 ( 내가 좋은 평가한 content )
        // 평가 이력이 없으면 유사 사용자를 찾을 수 없으므로 즉시 빈 목록 반환
        val myContentIds = fetchMyRatedContentIds(memberId, minRating = props.minRating)

        if (myContentIds.isEmpty()) return emptyList()

        // 2. 유사 사용자 조회
        // 내가 호평한 content 중 3개 이상 호평한 다른 member들을 추출
        // 내가 평가한 콘텐츠 중 rating >= minRating 으로 평가한 타 유저를 그룹화한다.
        // 공통 콘텐츠 수(co-count) >= SIMILAR_USER_MIN_CO_COUNT 인 유저만 유사 사용자로 판단하며,
        // co-count 내림차순으로 정렬해 상위 K명만 추출한다.
        val similarUserIds = jpaQueryFactory
            .select(qReview.member.id)
            .from(qReview)
            .where(
                qReview.content.id.`in`(myContentIds),              // 내가 평가한 콘텐츠를 평가한 유저
                qReview.rating.goe(props.minRating),                 // 최소 평점 이상만 유사도 계산에 포함
                qReview.member.id.ne(memberId)                       // 본인 제외
            )
            .groupBy(qReview.member.id)
            .having(qReview.count().goe(props.similarUserMinCoCount)) // 공통 콘텐츠 수 임계값 필터
            .orderBy(qReview.count().desc())                          // 공통 콘텐츠가 많을수록 유사도 높음
            .limit(props.similarUserK)                                // 상위 K명만 추출
            .fetch()
            .filterNotNull()

        if (similarUserIds.isEmpty()) return emptyList()

        val avgRating = qReview.rating.avg()
        val reviewCount = qReview.count()

        // 3. 유사 사용자의 고평점 콘텐츠 조회
        // 유사 사용자들이 rating >= CF_RECOMMEND_RATING 으로 평가한 콘텐츠를 JOIN 조건으로 필터링한다.
        // WHERE 절에서 내가 이미 평가한 콘텐츠(myContentIds)를 제외하고 미평가 콘텐츠만 반환한다.
        return jpaQueryFactory
            .select(qContent.id, qContent.title, qContent.type, qContent.thumbnailUrl, avgRating, reviewCount)
            .from(qContent)
            .join(qReview).on(
                qReview.content.id.eq(qContent.id),
                qReview.member.id.`in`(similarUserIds),          // 유사 사용자의 리뷰만 JOIN
                qReview.rating.goe(props.recommendRating)         // 추천 기준 평점 이상만 포함
            )
            .where(buildCfConditions(myContentIds, type))    // 미평가 + 삭제되지 않은 콘텐츠 필터
            .groupBy(qContent.id)
            .orderBy(avgRating.desc(), reviewCount.desc())   // 유사 사용자 사이 평균 평점 높은 순
            .limit(limit.toLong())
            .fetch()
            .map {
                ResponseRecommendationDto(
                    contentId = it.get(qContent.id)!!,
                    title = it.get(qContent.title)!!,
                    type = it.get(qContent.type)!!,
                    thumbnailUrl = it.get(qContent.thumbnailUrl),
                    avgRating = it.get(avgRating) ?: 0.0,
                    reviewCount = it.get(reviewCount)?.toInt() ?: 0,
                    recommendationType = RecommendationType.COLLABORATIVE_FILTERING
                )
            }
    }

    /**
     * 내가 평가한 콘텐츠 ID 목록 조회
     * member 가 평가한 rating 점수가 minRating 값 보다 크거나 같은 content id들을 추출
     *
     * @param minRating null이면 rating 조건 없음, 값이 있으면 rating >= minRating 필터 적용
     */
    private fun fetchMyRatedContentIds(memberId: Long, minRating: Int? = null): List<Long> =
        jpaQueryFactory
            .select(qReview.content.id)
            .from(qReview)
            .where(
                BooleanBuilder()
                    .and(qReview.member.id.eq(memberId))
                    .apply { minRating?.let { and(qReview.rating.goe(it)) } }
            )
            .fetch()
            .filterNotNull()

    /**
     * 인기도 쿼리 WHERE 조건 빌더
     *
     * - type이 null이 아니면 타입 필터 추가
     * - 내가 평가한 콘텐츠가 있으면 NOT IN 조건 추가
     */
    private fun buildPopularConditions(myContentIds: List<Long>, type: ContentType?): BooleanBuilder =
        BooleanBuilder().apply {
            type?.let { and(qContent.type.eq(it)) }
            if (myContentIds.isNotEmpty()) and(qContent.id.notIn(myContentIds))
        }

    /**
     * 협업 필터링 쿼리 WHERE 조건 빌더
     *
     * - 내가 이미 평가한 콘텐츠 NOT IN 고정
     * - type이 null이 아니면 타입 필터 추가
     */
    private fun buildCfConditions(myContentIds: List<Long>, type: ContentType?): BooleanBuilder =
        BooleanBuilder().apply {
            and(qContent.id.notIn(myContentIds))
            type?.let { and(qContent.type.eq(it)) }
        }
}
