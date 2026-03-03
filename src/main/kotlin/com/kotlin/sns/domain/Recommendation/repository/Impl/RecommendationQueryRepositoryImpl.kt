package com.kotlin.sns.domain.Recommendation.repository.Impl

import com.kotlin.sns.domain.Content.entity.ContentType
import com.kotlin.sns.domain.Content.entity.QContent
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
 */
@Repository
class RecommendationQueryRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory
) : RecommendationQueryRepository {

    private val qContent = QContent.content
    private val qReview = QReview.review

    companion object {
        const val SIMILAR_USER_K = 10L
        const val SIMILAR_USER_MIN_CO_COUNT = 3L
        const val CF_MIN_RATING = 3
        const val CF_RECOMMEND_RATING = 4
    }

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
                qReview.isDeleted.isFalse,
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
        val myContentIds = fetchMyRatedContentIds(memberId, minRating = CF_MIN_RATING)

        if (myContentIds.isEmpty()) return emptyList()

        val similarUserIds = jpaQueryFactory
            .select(qReview.member.id)
            .from(qReview)
            .where(
                qReview.content.id.`in`(myContentIds),
                qReview.rating.goe(CF_MIN_RATING),
                qReview.member.id.ne(memberId),
                qReview.isDeleted.isFalse
            )
            .groupBy(qReview.member.id)
            .having(qReview.count().goe(SIMILAR_USER_MIN_CO_COUNT))
            .orderBy(qReview.count().desc())
            .limit(SIMILAR_USER_K)
            .fetch()
            .filterNotNull()

        if (similarUserIds.isEmpty()) return emptyList()

        val avgRating = qReview.rating.avg()
        val reviewCount = qReview.count()

        return jpaQueryFactory
            .select(qContent.id, qContent.title, qContent.type, qContent.thumbnailUrl, avgRating, reviewCount)
            .from(qContent)
            .join(qReview).on(
                qReview.content.id.eq(qContent.id),
                qReview.member.id.`in`(similarUserIds),
                qReview.rating.goe(CF_RECOMMEND_RATING),
                qReview.isDeleted.isFalse
            )
            .where(buildCfConditions(myContentIds, type))
            .groupBy(qContent.id)
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
                    recommendationType = RecommendationType.COLLABORATIVE_FILTERING
                )
            }
    }

    /**
     * 내가 평가한 콘텐츠 ID 목록 조회
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
                    .and(qReview.isDeleted.isFalse)
                    .apply { minRating?.let { and(qReview.rating.goe(it)) } }
            )
            .fetch()
            .filterNotNull()

    /**
     * 인기도 쿼리 WHERE 조건 빌더
     *
     * - isDeleted=false 고정
     * - type이 null이 아니면 타입 필터 추가
     * - 내가 평가한 콘텐츠가 있으면 NOT IN 조건 추가
     */
    private fun buildPopularConditions(myContentIds: List<Long>, type: ContentType?): BooleanBuilder =
        BooleanBuilder().apply {
            and(qContent.isDeleted.isFalse)
            type?.let { and(qContent.type.eq(it)) }
            if (myContentIds.isNotEmpty()) and(qContent.id.notIn(myContentIds))
        }

    /**
     * 협업 필터링 쿼리 WHERE 조건 빌더
     *
     * - isDeleted=false 고정
     * - 내가 이미 평가한 콘텐츠 NOT IN 고정
     * - type이 null이 아니면 타입 필터 추가
     */
    private fun buildCfConditions(myContentIds: List<Long>, type: ContentType?): BooleanBuilder =
        BooleanBuilder().apply {
            and(qContent.isDeleted.isFalse)
            and(qContent.id.notIn(myContentIds))
            type?.let { and(qContent.type.eq(it)) }
        }
}
