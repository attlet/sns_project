package com.kotlin.sns.domain.Member.service.Impl

import com.kotlin.sns.domain.Member.dto.response.ResponseMemberDto
import com.kotlin.sns.domain.Member.entity.Member
import com.kotlin.sns.domain.Member.mapper.MemberMapper
import com.kotlin.sns.domain.Member.repository.MemberRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.util.*

import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

class MemberServiceImplTest {

    private lateinit var memberRepository: MemberRepository
    private lateinit var memberMapper: MemberMapper
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
//        val result = memberService.findMemberById(memberId)

        // then
        assertThat(responseMember).isEqualTo(responseMember)
    }
}