package com.kotlin.sns.domain.member.repository

import com.kotlin.sns.domain.Friend.const.FriendApplyStatusEnum
import com.kotlin.sns.domain.member.entity.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

/**
 * MemberRepository
 * member의 db 상호작용을 위한 repository
 */
interface MemberRepository : JpaRepository<Member, Long>, MemberRepositoryCustom{
    fun findByEmail(email : String) : Optional<Member>
//    fun findByUsername(username : String) : Optional<Member>
    fun findByUserId(userId : String) : Optional<Member>

    @Query("SELECT f.receiver.id " +
            "FROM Friend f " +
            "WHERE f.sender.id = :senderId AND f.status = :status")
    fun findFriendsId(senderId : Long, status : FriendApplyStatusEnum) : List<Long>
}