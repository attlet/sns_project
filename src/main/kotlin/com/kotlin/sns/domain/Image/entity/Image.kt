package com.kotlin.sns.domain.Image.entity

import com.kotlin.sns.common.entity.BaseEntity
import com.kotlin.sns.domain.Member.entity.Member
import com.kotlin.sns.domain.Posting.entity.Posting
import jakarta.persistence.*

@Entity
@Table(name = "image")
data class Image (
    @Column(name = "imageUrl", nullable = false)
    var imageUrl : String,

    @Enumerated(EnumType.STRING)
    @Column(name = "imageType", nullable = false)
    var imageType : ImageType,

    // Posting과의 관계 (게시글 첨부 이미지인 경우)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "postingId")
    var posting: Posting? = null,

    // Member와의 관계 (프로필 이미지인 경우)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberId")
    var member: Member? = null
) : BaseEntity(){
}

/**
 * image의 타입 enum
 *
 */
enum class ImageType {
    PROFILE,
    IN_POSTING
}