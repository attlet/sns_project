package com.kotlin.sns.domain.Member.repository

import com.kotlin.sns.domain.Member.entity.Member
import org.springframework.data.jpa.repository.JpaRepository

interface memberRepository : JpaRepository<Member, Long>{

}