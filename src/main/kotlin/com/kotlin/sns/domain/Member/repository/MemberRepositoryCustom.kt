package com.kotlin.sns.domain.Member.repository

import com.kotlin.sns.domain.Member.entity.Member
import java.util.Optional


interface MemberRepositoryCustom {
    fun findByUserId(userId : String) : Optional<Member>
}