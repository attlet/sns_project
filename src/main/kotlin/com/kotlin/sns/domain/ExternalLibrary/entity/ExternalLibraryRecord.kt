package com.kotlin.sns.domain.ExternalLibrary.entity

import com.kotlin.sns.common.entity.BaseEntity
import com.kotlin.sns.domain.Content.entity.Content
import com.kotlin.sns.domain.Member.entity.Member
import com.kotlin.sns.domain.Review.entity.ReviewSource
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
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction
import java.time.Instant

/**
 * 외부 라이브러리 동기화 레코드 엔티티
 *
 * Steam, MAL, AniList 등 외부 플랫폼에서 동기화한 원본 메타데이터를 보관한다.
 * Review 엔티티에서 분리된 외부 플랫폼 전용 데이터(playtime, externalRating, syncedAt)를 관리한다.
 *
 * @property member 소유 회원
 * @property content 대상 작품
 * @property source 데이터 출처 (STEAM, MAL, ANILIST — MANUAL 없음)
 * @property playtimeMinutes 게임 플레이 시간 (분 단위, 게임 외 콘텐츠는 null)
 * @property externalRating 외부 플랫폼 원본 점수
 * @property syncedAt 마지막 외부 동기화 시간
 * @property isDeleted 삭제 여부 (Soft Delete)
 */
@Entity
@SQLDelete(sql = "UPDATE external_library_record SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Table(
    name = "external_library_record",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_external_library_member_content_source",
            columnNames = ["member_id", "content_id", "source"]
        )
    ],
    indexes = [
        Index(name = "idx_external_library_member", columnList = "member_id"),
        Index(name = "idx_external_library_content", columnList = "content_id"),
        Index(name = "idx_external_library_source", columnList = "source")
    ]
)
class ExternalLibraryRecord(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    var member: Member,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    var content: Content,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var source: ReviewSource,

    var playtimeMinutes: Int? = null,

    var externalRating: Double? = null,

    @Column(nullable = false)
    var syncedAt: Instant

) : BaseEntity()
