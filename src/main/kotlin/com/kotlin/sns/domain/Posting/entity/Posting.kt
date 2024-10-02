package com.kotlin.sns.domain.Posting.entity

import com.kotlin.sns.common.entity.BaseEntity
import com.kotlin.sns.domain.Member.entity.Member
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

/**
 * post entity class
 *
 * id (PK): 포스팅 고유 식별자
 * writerId (FK): 작성자 (User 엔티티의 FK)
 * content: 포스팅 내용
 * image_url: 이미지 경로 (이미지 첨부 안 할수도 있음)
 * created_at: 포스팅 작성 일시
 * updated_at: 포스팅 수정 일시
 *
 * @property content
 * @property imageUrl
 * @property member
 */
@Entity
@Table(name = "posting")
data class Posting(
    @Column(nullable = false, columnDefinition = "TEXT")
    val content : String,

    val imageUrl : String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writerId")
    val member : Member
) : BaseEntity()