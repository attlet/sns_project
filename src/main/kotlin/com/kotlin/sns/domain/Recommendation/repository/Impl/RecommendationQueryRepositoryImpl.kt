package com.kotlin.sns.domain.Recommendation.repository.Impl

import com.kotlin.sns.domain.Content.entity.ContentType
import com.kotlin.sns.domain.Content.entity.QContent
import com.kotlin.sns.domain.Recommendation.dto.response.RecommendationType
import com.kotlin.sns.domain.Recommendation.dto.response.ResponseRecommendationDto
import com.kotlin.sns.domain.Recommendation.repository.RecommendationQueryRepository
import com.kotlin.sns.domain.Review.entity.QReview
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
     * 1. 내가 평가한 content_id 목록 조회
     * 2. LEFT JOIN review (isDeleted=false, rating IS NOT NULL)
     * 3. type 필터, 미평가 콘텐츠 필터, HAVING COUNT >= 1
     * 4. AVG(rating) DESC, COUNT DESC 정렬
     */
    override fun findPopularContents(memberId: Long, type: ContentType?, limit: Int): List<ResponseRecommendationDto> {
        val myContentIds = jpaQueryFactory
            .select(qReview.content.id)
            .from(qReview)
            .where(qReview.member.id.eq(memberId), qReview.isDeleted.isFalse)
            .fetch()
            .filterNotNull()

        val avgRatingExpr = qReview.rating.avg()
        val countExpr = qReview.count()

        val conditions = listOfNotNull(
            qContent.isDeleted.isFalse,
            type?.let { qContent.type.eq(it) },
            if (myContentIds.isEmpty()) null else qContent.id.notIn(myContentIds)
        )

        val results = jpaQueryFactory
            .select(qContent.id, qContent.title, qContent.type, qContent.thumbnailUrl, avgRatingExpr, countExpr)
            .from(qContent)
            .leftJoin(qReview).on(
                qReview.content.id.eq(qContent.id),
                qReview.isDeleted.isFalse,
                qReview.rating.isNotNull
            )
            .where(*conditions.toTypedArray())
            .groupBy(qContent.id)
            .having(countExpr.gt(0))
            .orderBy(avgRatingExpr.desc(), countExpr.desc())
            .limit(limit.toLong())
            .fetch()

        return results.map {
            ResponseRecommendationDto(
                contentId = it.get(qContent.id)!!,
                title = it.get(qContent.title)!!,
                type = it.get(qContent.type)!!,
                thumbnailUrl = it.get(qContent.thumbnailUrl),
                avgRating = it.get(avgRatingExpr) ?: 0.0,
                reviewCount = it.get(countExpr)?.toInt() ?: 0,
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
        val myContentIds = jpaQueryFactory
            .select(qReview.content.id)
            .from(qReview)
            .where(
                qReview.member.id.eq(memberId),
                qReview.rating.goe(CF_MIN_RATING),
                qReview.isDeleted.isFalse
            )
            .fetch()
            .filterNotNull()

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

        val avgRatingExpr = qReview.rating.avg()
        val countExpr = qReview.count()

        val conditions = listOfNotNull(
            qContent.isDeleted.isFalse,
            qContent.id.notIn(myContentIds),
            type?.let { qContent.type.eq(it) }
        )

        val results = jpaQueryFactory
            .select(qContent.id, qContent.title, qContent.type, qContent.thumbnailUrl, avgRatingExpr, countExpr)
            .from(qContent)
            .join(qReview).on(
                qReview.content.id.eq(qContent.id),
                qReview.member.id.`in`(similarUserIds),
                qReview.rating.goe(CF_RECOMMEND_RATING),
                qReview.isDeleted.isFalse
            )
            .where(*conditions.toTypedArray())
            .groupBy(qContent.id)
            .orderBy(avgRatingExpr.desc(), countExpr.desc())
            .limit(limit.toLong())
            .fetch()

        return results.map {
            ResponseRecommendationDto(
                contentId = it.get(qContent.id)!!,
                title = it.get(qContent.title)!!,
                type = it.get(qContent.type)!!,
                thumbnailUrl = it.get(qContent.thumbnailUrl),
                avgRating = it.get(avgRatingExpr) ?: 0.0,
                reviewCount = it.get(countExpr)?.toInt() ?: 0,
                recommendationType = RecommendationType.COLLABORATIVE_FILTERING
            )
        }
    }
}
