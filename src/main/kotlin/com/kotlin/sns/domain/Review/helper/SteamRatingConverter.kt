package com.kotlin.sns.domain.Review.helper

import org.springframework.stereotype.Component

/**
 * Steam 플레이 시간 → rating 변환 컴포넌트
 *
 * playtimeMinutes 값을 1~5 별점으로 변환한다.
 * 변환 구간은 SteamRatingProperties를 통해 YAML에서 주입되며, 기본값은 아래와 같다.
 *
 * | 플레이 시간           | rating | 근거               |
 * |---------------------|--------|------------------|
 * | 0분 (미플레이)        | null   | 소유만 함            |
 * | 1 ~ 599분 (10시간 미만)  | 2      | 조금 해봄           |
 * | 600 ~ 2999분 (10~50시간) | 3      | 플레이함             |
 * | 3000 ~ 11999분 (50~200시간) | 4   | 꽤 좋아함           |
 * | 12000분+ (200시간+)   | 5      | 매우 좋아함          |
 *
 * @property properties 변환 구간 설정
 */
@Component
class SteamRatingConverter(
    private val properties: SteamRatingProperties
) {

    /**
     * playtimeMinutes를 rating으로 변환한다.
     *
     * @param playtimeMinutes 플레이 시간 (분 단위, 0 이상)
     * @return 별점 (1~5), 미플레이(0분)인 경우 null
     */
    fun convert(playtimeMinutes: Int): Int? {
        return when {
            playtimeMinutes == 0 -> null
            playtimeMinutes <= properties.thresholds.tier2Max -> 2
            playtimeMinutes <= properties.thresholds.tier3Max -> 3
            playtimeMinutes <= properties.thresholds.tier4Max -> 4
            else -> 5
        }
    }
}
