package com.kotlin.sns.domain.Notification.repository

import com.kotlin.sns.domain.Member.entity.Member
import com.kotlin.sns.domain.Notification.entity.Notification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface NotificationRepository : JpaRepository<Notification, Long> {
    fun findAllByReceiver(receiver: Member): List<Notification>
}