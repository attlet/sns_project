package com.kotlin.sns.domain.Hashtag.entity

import com.kotlin.sns.common.entity.BaseEntity
import com.kotlin.sns.domain.Posting.entity.Posting
import com.kotlin.sns.domain.PostingHashtag.entity.PostingHashtag
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany

@Entity
data class Hashtag(
    @Column(name = "tagName", nullable = false)
    var tagName : String,

    @OneToMany(mappedBy = "hashtag", cascade = [CascadeType.REMOVE])
    var postingHashtag: MutableList<PostingHashtag> = mutableListOf()
) : BaseEntity() {
}