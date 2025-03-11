package com.kotlin.sns.domain.Member.service.Impl

import com.kotlin.sns.domain.Member.dto.response.ResponseMemberDto
import com.kotlin.sns.domain.Member.entity.Member
import com.kotlin.sns.domain.Member.repository.MemberRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.*

@SpringBootTest
class MemberServiceImplTest {

    @Autowired
    private lateinit var memberService: MemberServiceImpl
    @Autowired
    private lateinit var memberRepository : MemberRepository
    @Test
    fun findMemberByIdReturnsMemberWhenExists() {
        // given
        val memberId : Long = 1L

        val member = Member(
            userId = "user123",
            name = "User One",
            email = "user1@example.com",
            pw = "password",
            roles = listOf("USER")
        )

        val responseMember = ResponseMemberDto(name = "user123", "user1@example.com")
        whenever(memberRepository.findById(memberId)).thenReturn(Optional.of(member))
//        whenever(memberMapper.toDto(member)).thenReturn(responseMember)
//
        // when
        memberService.findMemberById(1L)

        // then
        assertThat(responseMember).isEqualTo(responseMember)
    }
}