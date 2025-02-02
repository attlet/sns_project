package com.kotlin.sns.domain.Friend.repository

interface friendRepositoryCustom {
    fun isFriendRequestExist(receiverId : Long, senderId : Long) : Boolean
}