package com.kotlin.sns.domain.Sse.ConnectionRegistry

interface ConnectionRegistry {
    fun register(userId: Long, instanceId: String)
    fun getManagingInstance(userId: Long): String?
    fun unregister(userId: Long)
}