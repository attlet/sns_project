package com.kotlin.sns.common.entity

import jakarta.persistence.Column
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import jakarta.persistence.EntityListeners
import java.time.Instant

/*

엔티티들이 공통적으로 가지는 속성.
open를 통해 상속이 가능하도록 선언

인스턴스화 시키지 않고, 상속만 허용함으로써 명확하게 공통 속성 상속이라는
구조를 유지

*/
@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
open class BaseEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, updatable = false)
    @CreatedDate
    var createdDt: Instant = Instant.now(),

    @Column(nullable = false)
    @LastModifiedDate
    var updateDt: Instant = Instant.now(),

    @Column(nullable = false)
    var isDeleted: Boolean = false
)