package com.kotlin.sns.domain.Friend.repository

import com.kotlin.sns.domain.Friend.entity.Friend
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface friendRepository : JpaRepository<Friend, Long>, friendRepositoryCustom {
}