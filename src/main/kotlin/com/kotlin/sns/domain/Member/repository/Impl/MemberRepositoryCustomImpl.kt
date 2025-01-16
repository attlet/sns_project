package com.kotlin.sns.domain.Member.repository.Impl

import com.kotlin.sns.domain.Friend.const.friendApplyStatusEnum
import com.kotlin.sns.domain.Friend.entity.QFriend
import com.kotlin.sns.domain.Member.repository.MemberRepositoryCustom
import com.kotlin.sns.domain.Notification.entity.NotificationType
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class MemberRepositoryCustomImpl(
    private val jpaQueryFactory: JPAQueryFactory
) : MemberRepositoryCustom {

    private val qFriend = QFriend.friend

    /**
     * sender와 친구관계인 member들의 id값 조회
     *
     * @param senderId
     * @return
     */
    override fun findFriendsId(senderId: Long): List<Long> {
        return jpaQueryFactory
            .select(qFriend.receiver.id)
            .from(qFriend)
            .where(qFriend.sender.id.eq(senderId))
            .fetch()
    }
}