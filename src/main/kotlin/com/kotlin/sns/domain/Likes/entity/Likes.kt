package com.kotlin.sns.domain.Likes.entity

import com.kotlin.sns.common.entity.BaseEntity
import com.kotlin.sns.domain.Member.entity.Member
import com.kotlin.sns.domain.Posting.entity.Posting
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "likes")
data class Likes(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "postingId")
    private val posting: Posting,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberId")
    private val member: Member
) :BaseEntity() {
}