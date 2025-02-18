package com.kotlin.sns.domain.member.entity

import com.kotlin.sns.common.entity.BaseEntity
import com.kotlin.sns.domain.Image.entity.Image
import com.kotlin.sns.domain.Posting.entity.Posting
import jakarta.persistence.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails


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
 * @property pw
 * @property profileImageUrl
 * @property postings
 */
@Entity
@Table(name = "member")
data class Member(
    @Column(nullable = false, unique = true)
    var userId : String,

    @Column(nullable = false, unique = true)
    var name: String,

    @Column(nullable = false, unique = true)
    var email: String,

    @Column(nullable = false, unique = true)
    var pw: String,

    @OneToMany(mappedBy = "member", cascade = [CascadeType.REMOVE])
    var postings: List<Posting> = mutableListOf(),

    @OneToMany(mappedBy = "member", cascade = [CascadeType.REMOVE])
    var profileImageUrl: List<Image>? = mutableListOf(),

    @ElementCollection(fetch = FetchType.EAGER)
    var roles: List<String>
) : BaseEntity(), UserDetails {
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return roles.map { SimpleGrantedAuthority(it) }.toMutableList()
    }

    override fun getPassword(): String {
        return pw
    }

    override fun getUsername(): String {
        return userId
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return true
    }
}

