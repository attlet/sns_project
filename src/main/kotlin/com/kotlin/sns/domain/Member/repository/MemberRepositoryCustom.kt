package com.kotlin.sns.domain.Member.repository

interface MemberRepositoryCustom {
    fun findFriendsId(senderId : Long) : List<Long>
}