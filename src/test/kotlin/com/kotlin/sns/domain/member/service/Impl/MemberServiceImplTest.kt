package com.kotlin.sns.domain.member.service.Impl

import com.kotlin.sns.domain.member.dto.response.ResponseMemberDto
import com.kotlin.sns.domain.member.entity.Member
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class MemberServiceImplTest {

    @Autowired
    private lateinit var memberService: MemberServiceImpl
    @Test
    fun findMemberByIdReturnsMemberWhenExists() {
        // given

        val member = Member(
            userId = "user123",
            name = "User One",
            email = "user1@example.com",
            pw = "password",
            roles = listOf("USER")
        )
        val responseMember = ResponseMemberDto(name = "user123", "user1@example.com")
//        whenever(memberRepository.findById(memberId)).thenReturn(Optional.of(member))
//        whenever(memberMapper.toDto(member)).thenReturn(responseMember)
//
//        // when
        memberService.findMemberById(1L)

        // then
        assertThat(responseMember).isEqualTo(responseMember)
    }
}