package com.kotlin.sns.domain.Member.entity

import com.kotlin.sns.common.entity.BaseEntity
import com.kotlin.sns.domain.Member.dto.request.RequestUpdateMemberDto
import com.kotlin.sns.domain.Post.entity.Posting
import jakarta.persistence.*


/**
 * member entity class
 *
 * id (PK): 사용자 고유 식별자
 * name: 사용자 이름
 * email: 이메일 주소
 * password: 비밀번호 (암호화되어 저장)
 * profile_image_url: 프로필 이미지 경로 (프로필 없으면 기본 이미지)
 * created_at: 생성 일시
 * updated_at: 정보 수정 일시
 *
 * post와 1:n 관계 (cascade 옵션 all, fetchtype = lazy 사용)
 *
 * @property name
 * @property email
 * @property password
 * @property profileImageUrl
 * @property postings
 */
@Entity
@Table(name = "member")
data class Member (
    @Column(nullable = false, unique = true)
    val name : String,

    @Column(nullable = false, unique = true)
    val email : String,

    @Column(nullable = false, unique = true)
    val password : String,

    val profileImageUrl : String? = null,

    @OneToMany(mappedBy = "member", cascade = [CascadeType.ALL])
    val postings : List<Posting> = mutableListOf()
) : BaseEntity() {

}

