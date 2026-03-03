package com.kotlin.sns.domain.Review.helper

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Steam 플레이 시간 → rating 변환 구간 설정
 *
 * application.yml의 `external.steam.rating` 프리픽스에서 타입 안전하게 바인딩된다.
 * 기본값은 data class 필드에 명시되어 있으며, YAML 미설정 시 기본값이 사용된다.
 *
 * 변환 구간 (분 단위):
 *   0분          → null  (미플레이)
 *   1 ~ tier2Max → 2
 *   (tier2Max+1) ~ tier3Max → 3
 *   (tier3Max+1) ~ tier4Max → 4
 *   (tier4Max+1)+  → 5
 *
 * @property thresholds 각 tier의 최대값 임계값
 */
@ConfigurationProperties(prefix = "external.steam.rating")
data class SteamRatingProperties(
    val thresholds: Thresholds = Thresholds()
) {
    data class Thresholds(
        val tier2Max: Int = 599,
        val tier3Max: Int = 2999,
        val tier4Max: Int = 11999
    )
}
