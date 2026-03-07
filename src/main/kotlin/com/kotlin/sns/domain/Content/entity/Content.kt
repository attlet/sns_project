package com.kotlin.sns.domain.Content.entity

import com.kotlin.sns.common.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction

/**
 * 게임/만화/애니를 통합 관리하는 Content 엔티티
 *
 * @property type 콘텐츠 타입 (GAME, MANGA, ANIME)
 * @property title 제목
 * @property description 설명
 * @property releaseYear 출시년도
 * @property thumbnailUrl 썸네일 이미지 URL (외부 링크)
 * @property steamAppId Steam 앱 ID (외부 연동용)
 * @property igdbId IGDB ID (외부 연동용)
 * @property malId MyAnimeList ID (외부 연동용)
 * @property anilistId AniList ID (외부 연동용)
 * @property isDeleted 삭제 여부 (Soft Delete)
 */
@Entity
@SQLDelete(sql = "UPDATE content SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Table(
    name = "content",
    indexes = [
        Index(name = "idx_content_title", columnList = "title"),
        Index(name = "idx_content_type", columnList = "type")
    ]
)
class Content(
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var type: ContentType,

    @Column(nullable = false)
    var title: String,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    var releaseYear: Int? = null,

    @Column(length = 500)
    var thumbnailUrl: String? = null,

    @Column(unique = true)
    var steamAppId: Long? = null,

    @Column(unique = true)
    var igdbId: Long? = null,

    @Column(unique = true)
    var malId: Long? = null,

    @Column(unique = true)
    var anilistId: Long? = null

) : BaseEntity()
