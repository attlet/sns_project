package com.kotlin.sns.domain.Member.repository

import com.kotlin.sns.domain.Member.entity.Member
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

/**
 * MemberRepository
 * member의 db 상호작용을 위한 repository
 */
interface MemberRepository : JpaRepository<Member, Long>{
    fun findByEmail(email : String) : Optional<Member>
}