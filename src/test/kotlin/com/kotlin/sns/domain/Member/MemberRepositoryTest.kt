package com.kotlin.sns.domain.Member

import com.kotlin.sns.domain.Member.entity.Member
import com.kotlin.sns.domain.Member.repository.MemberRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
class MemberRepositoryTest {

    @Autowired
    lateinit var memberRepository : MemberRepository

    @Test
    fun findByEmailTest(){
        //given
        val member1 = Member(
            userId = "abcd",
            name = "user1",
            email = "aaa@naver.com",
            pw = "1234",
            roles = listOf("user1"))

        memberRepository.save(member1)

        //when
        val findMember = memberRepository.findByEmail("aaa@naver.com")

        //then
        assertThat(findMember)
            .isPresent
            .hasValueSatisfying {
                assertThat(it.userId).isEqualTo("abcd")
                assertThat(it.name).isEqualTo("user1")
            }
    }

    @Test
    fun saveTest() {
        // given
        val member = Member(
            userId = "testuser",
            name = "Test User",
            email = "test@example.com",
            pw = "password",
            roles = listOf("USER")
        )

        // when
        val savedMember = memberRepository.save(member)

        // then
        assertThat(savedMember.id).isNotNull()
        assertThat(savedMember.userId).isEqualTo("testuser")
        assertThat(savedMember.email).isEqualTo("test@example.com")
    }

    @Test
    fun findByUserIdTest() {
        // given
        val member = Member(
            userId = "testuser",
            name = "Test User",
            email = "test@example.com",
            pw = "password",
            roles = listOf("USER")
        )
        memberRepository.save(member)

        // when
        val foundMember = memberRepository.findByUserId("testuser")

        // then
        assertThat(foundMember).isPresent
        assertThat(foundMember.get().userId).isEqualTo("testuser")
    }
}