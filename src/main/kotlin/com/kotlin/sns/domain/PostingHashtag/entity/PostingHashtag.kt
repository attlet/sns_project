package com.kotlin.sns.domain.PostingHashtag.entity

import com.kotlin.sns.common.entity.BaseEntity
import com.kotlin.sns.domain.Hashtag.entity.Hashtag
import com.kotlin.sns.domain.Posting.entity.Posting
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity
data class PostingHashtag(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "postingId")
    var posting: Posting,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hashtagId")
    var hashtag: Hashtag
) : BaseEntity() {
}