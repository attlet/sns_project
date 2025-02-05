package com.kotlin.sns.member

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
            .isEqualTo(member1)
    }

}