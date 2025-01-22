package com.kotlin.sns.domain.Friend.entity

import com.kotlin.sns.common.entity.BaseEntity
import com.kotlin.sns.domain.Friend.const.FriendApplyStatusEnum
import com.kotlin.sns.domain.Member.entity.Member
import jakarta.persistence.*

/**
 * friend entity class
 *
 * id (FK): 사용자 (User 엔티티의 FK)
 * friendId (FK): 친구 사용자 (User 엔티티의 FK)
 * status: 친구 상태 (예: 요청 중, 수락됨, 차단됨)
 * created_at: 친구 요청 일시
 * updated_at: 상태 변경 일시
 *
 * user 와 user 를 다대다 관계 연결
 *
 * @property sender
 * @property receiver
 * @property status
 */
@Entity
@Table(name = "friend")
class Friend (
    @ManyToOne
    @JoinColumn(name = "senderId")
    var sender : Member,

    @ManyToOne
    @JoinColumn(name = "receiverId")
    var receiver : Member,

    @Enumerated(EnumType.STRING)  //enum 타입 값을 엔티티 클래스 속성에 사용하도록 지정
    var status : FriendApplyStatusEnum
) : BaseEntity(){
}
