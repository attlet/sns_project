package com.kotlin.sns.domain.Member.repository.Impl


import com.kotlin.sns.domain.Friend.entity.QFriend
import com.kotlin.sns.domain.Member.repository.MemberRepositoryCustom
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class MemberRepositoryCustomImpl(
    private val jpaQueryFactory: JPAQueryFactory
) : MemberRepositoryCustom {

    private val qFriend = QFriend.friend


}