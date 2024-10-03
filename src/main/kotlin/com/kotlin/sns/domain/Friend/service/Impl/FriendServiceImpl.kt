package com.kotlin.sns.domain.Friend.service.Impl

import com.kotlin.sns.domain.Friend.repository.friendRepository
import com.kotlin.sns.domain.Friend.service.FriendService
import org.springframework.stereotype.Service

/**
 * friend 비즈니스 로직 처리
 *
 * @property friendRepository
 */
@Service
class FriendServiceImpl(
    private val friendRepository: friendRepository
) : FriendService{
    override fun findFriendById() {
        TODO("Not yet implemented")
    }

    override fun createFriend() {
        TODO("Not yet implemented")
    }

    override fun updateFriend() {
        TODO("Not yet implemented")
    }

    override fun deleteFriend() {
        TODO("Not yet implemented")
    }

}