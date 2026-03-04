package com.kotlin.sns.domain.Recommendation.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * 추천 시스템 알고리즘 튜닝 파라미터
 *
 * application.yml의 `recommendation.cf` 프리픽스에서 바인딩된다.
 * 기본값은 필드에 명시되어 있으며, YAML 미설정 시 기본값이 사용된다.
 *
 * @property similarUserK 유사 사용자 최대 추출 수 (상위 K명)
 * @property similarUserMinCoCount 유사 사용자로 인정하기 위한 최소 공통 콘텐츠 수
 * @property minRating 유사도 계산에 포함할 최소 평점
 * @property recommendRating 추천 기준 최소 평점 (유사 사용자가 이 점수 이상 준 콘텐츠만 추천)
 */
@ConfigurationProperties(prefix = "recommendation.cf")
data class RecommendationProperties(
    val similarUserK: Long = 10L,
    val similarUserMinCoCount: Long = 3L,
    val minRating: Int = 3,
    val recommendRating: Int = 4
)
