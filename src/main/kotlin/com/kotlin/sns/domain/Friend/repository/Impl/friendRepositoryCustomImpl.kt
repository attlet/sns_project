package com.kotlin.sns.domain.Friend.repository.Impl

import com.kotlin.sns.domain.Friend.entity.Friend
import com.kotlin.sns.domain.Friend.entity.QFriend
import com.kotlin.sns.domain.Friend.repository.friendRepositoryCustom
import com.querydsl.jpa.impl.JPAQueryFactory

class friendRepositoryCustomImpl(
    private val jpaQueryFactory: JPAQueryFactory
) : friendRepositoryCustom {

    val qFriend = QFriend.friend

    /**
     * 이전에 sender에게 친구 요청을 했었는지 체크
     *
     * @param receiverId
     * @param senderId
     * @return
     */
    override fun isFriendRequestExist(receiverId : Long, senderId : Long): Boolean {
        return jpaQueryFactory
            .selectFrom(qFriend)
            .where(qFriend.sender.id.eq(senderId)
                .and(qFriend.receiver.id.eq(receiverId)))
            .fetchFirst() != null
    }
}