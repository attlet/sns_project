package com.kotlin.sns.domain.Post.entity

import com.kotlin.sns.common.entity.BaseEntity
import com.kotlin.sns.domain.Member.entity.Member
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "post")
class Post(
    val title : String,
    val detail : String,

    @ManyToOne
    @JoinColumn(name = "writerId")
    val member : Member
) : BaseEntity()