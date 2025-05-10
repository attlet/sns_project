package com.kotlin.sns.domain.Member.service.Impl

import com.kotlin.sns.common.exception.CustomException
import com.kotlin.sns.domain.Member.dto.request.RequestCreateMemberDto
import com.kotlin.sns.domain.Member.entity.Member
import com.kotlin.sns.domain.Member.mapper.MemberMapper
import com.kotlin.sns.domain.Member.repository.MemberRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import java.util.*

@SpringBootTest
class MemberServiceImplTest {

    @Autowired
    private lateinit var memberService: MemberServiceImpl
    
    @MockBean
    private lateinit var memberRepository: MemberRepository

    @Test
    fun `findMemberById should return member when exists`() {
        // given
        val memberId = 1L
        val member = Member(
            userId = "user123",
            name = "User One",
            email = "user1@example.com",
            pw = "password",
            roles = listOf("USER")
        )
        whenever(memberRepository.findById(memberId)).thenReturn(Optional.of(member))

        // when
        val result = memberService.findMemberById(memberId)

        // then
        assertThat(result.name).isEqualTo(member.name)
        assertThat(result.userId).isEqualTo(member.userId)
    }

    @Test
    fun `findMemberById should throw exception when member not found`() {
        // given
        val memberId = 1L
        whenever(memberRepository.findById(memberId)).thenReturn(Optional.empty())

        // when & then
        val exception = assertThrows(CustomException::class.java) {
            memberService.findMemberById(memberId)
        }
        assertThat(exception.message).contains("Member with id $memberId not found")
    }

    @Test
    fun `createMember should save and return member`() {
        // given
        val request = RequestCreateMemberDto(
            userId = "user123",
            name = "User One",
            email = "user1@example.com",
            pw = "password"
        )
        val member = Member(
            userId = request.userId,
            name = request.name,
            email = request.email,
            pw = request.pw,
            roles = listOf("user")
        )
        whenever(memberRepository.save(Mockito.any(Member::class.java))).thenReturn(member)

        // when
        val result = memberService.createMember(request)

        // then
        assertThat(result.name).isEqualTo(request.name)
        assertThat(result.userId).isEqualTo(request.userId)
    }

    @Test
    fun `deleteMember should remove member when exists`() {
        // given
        val memberId = 1L
        whenever(memberRepository.existsById(memberId)).thenReturn(true)

        // when
        memberService.deleteMember(memberId)

        // then
        Mockito.verify(memberRepository).deleteById(memberId)
    }

    @Test
    fun `deleteMember should throw exception when member not found`() {
        // given
        val memberId = 1L
        whenever(memberRepository.existsById(memberId)).thenReturn(false)

        // when & then
        val exception = assertThrows(CustomException::class.java) {
            memberService.deleteMember(memberId)
        }
        assertThat(exception.message).contains("Member with id $memberId not found")
    }
}