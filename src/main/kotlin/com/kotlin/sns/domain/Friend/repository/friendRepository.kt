package com.kotlin.sns.domain.Friend.repository

import com.kotlin.sns.domain.Friend.entity.Friend
import org.springframework.data.jpa.repository.JpaRepository

interface friendRepository : JpaRepository<Friend, Long> {
}