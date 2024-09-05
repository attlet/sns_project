package com.kotlin.sns.domain.Member.entity

import com.kotlin.sns.common.entity.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "member")
class Member (
    val name : String,
    val email : String
) : BaseEntity()

