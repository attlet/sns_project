package com.kotlin.sns.domain.Review.entity

import com.kotlin.sns.common.entity.BaseEntity
import com.kotlin.sns.domain.Content.entity.Content
import com.kotlin.sns.domain.Member.entity.Member
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant

/**
 * 작품 평가(Review) 엔티티
 *
 * 사용자(Member)가 작품(Content)에 대해 남긴 평가 정보를 관리한다.
 * 하나의 Member는 하나의 Content에 대해 하나의 Review만 가질 수 있다.
 *
 * @property member 평가자
 * @property content 평가 대상 작품
 * @property rating 별점 (1~5)
 * @property status 작품 상태 (PLAYING, PLAYED, DROPPED, WISHLIST, FAVORITE)
 * @property comment 한줄평 (선택, 최대 200자)
 * @property source 데이터 출처 (기본값: MANUAL)
 * @property externalRating 외부 플랫폼 원본 점수
 * @property playtime 게임 플레이 시간 (분 단위)
 * @property syncedAt 마지막 외부 동기화 시간
 * @property isDeleted 삭제 여부 (Soft Delete)
 */
@Entity
@Table(
    name = "review",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_review_member_content",
            columnNames = ["member_id", "content_id"]
        )
    ],
    indexes = [
        Index(name = "idx_review_member", columnList = "member_id"),
        Index(name = "idx_review_content", columnList = "content_id"),
        Index(name = "idx_review_status", columnList = "status")
    ]
)
class Review(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    var member: Member,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    var content: Content,

    @Column(nullable = false)
    var rating: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: ReviewStatus,

    @Column(length = 200)
    var comment: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var source: ReviewSource = ReviewSource.MANUAL,

    var externalRating: Double? = null,

    var playtime: Int? = null,

    var syncedAt: Instant? = null,

    @Column(nullable = false)
    var isDeleted: Boolean = false

) : BaseEntity()
