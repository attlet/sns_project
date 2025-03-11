package com.kotlin.sns.domain.Comment.entity

import com.kotlin.sns.common.entity.BaseEntity
import com.kotlin.sns.domain.Member.entity.Member
import com.kotlin.sns.domain.Posting.entity.Posting
import jakarta.persistence.*


@Entity
@Table(name = "comment")
data class Comment(
    var content : String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writerId")
    var member : Member,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "postingId")
    var posting: Posting
) : BaseEntity(){
}