package com.kotlin.sns.domain.Member.service.Impl

import com.kotlin.sns.common.exception.CustomException
import com.kotlin.sns.common.exception.ErrorCode
import com.kotlin.sns.domain.Member.dto.request.RequestCreateMemberDto
import com.kotlin.sns.domain.Member.dto.request.RequestUpdateMemberDto
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

/**
 * MemberServiceImpl 테스트 클래스
 *
 */
@SpringBootTest
class MemberServiceImplTest {

    @Autowired
    private lateinit var memberService: MemberServiceImpl
    
    @MockBean
    private lateinit var memberRepository: MemberRepository

    /**
     * 회원 ID로 회원을 조회합니다.
     * - given: 회원 ID와 해당 ID로 조회될 회원 객체를 설정합니다.
     * - when: 설정된 회원 ID로 `findMemberById` 메소드를 호출합니다.
     * - then: 반환된 회원의 이름과 사용자 ID가 예상과 일치하는지 확인합니다.
     */
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

    /**
     * 존재하지 않는 회원 ID로 조회 시 예외가 발생하는지 테스트합니다.
     * - given: 존재하지 않는 회원 ID를 설정하고, 해당 ID로 조회 시 빈 Optional 객체를 반환하도록 설정합니다.
     * - when: 설정된 회원 ID로 `findMemberById` 메소드를 호출합니다.
     * - then: `CustomException`이 발생하고, 예외 메시지가 예상과 일치하는지 확인합니다.
     */
    @Test
    fun `findMemberById should throw exception when member not found`() {
        // given
        val memberId = 1L
        whenever(memberRepository.findById(memberId)).thenReturn(Optional.empty())

        // when & then
        val exception = assertThrows(CustomException::class.java) {
            memberService.findMemberById(memberId)
        }
        assertThat(exception.message).contains(ErrorCode.MEMBER_NOT_FOUND.message)
    }

    /**
     * 새로운 회원을 생성합니다.
     * - given: 회원 생성을 위한 요청 DTO와 생성될 회원 객체를 설정합니다.
     * - when: `createMember` 메소드를 호출하여 회원을 생성합니다.
     * - then: 반환된 회원의 이름과 사용자 ID가 요청 DTO의 값과 일치하는지 확인합니다.
     */
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

    /**
     * 존재하는 회원을 삭제합니다.
     * - given: 존재하는 회원 ID를 설정합니다.
     * - when: `deleteMember` 메소드를 호출하여 회원을 삭제합니다.
     * - then: `memberRepository.deleteById`가 호출되었는지 확인합니다.
     */
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

    /**
     * 존재하지 않는 회원을 삭제 시 예외가 발생하는지 테스트합니다.
     * - given: 존재하지 않는 회원 ID를 설정합니다.
     * - when: `deleteMember` 메소드를 호출합니다.
     * - then: `CustomException`이 발생하고, 예외 메시지가 예상과 일치하는지 확인합니다.
     */
    @Test
    fun `deleteMember should throw exception when member not found`() {
        // given
        val memberId = 1L
        whenever(memberRepository.existsById(memberId)).thenReturn(false)

        // when & then
        val exception = assertThrows(CustomException::class.java) {
            memberService.deleteMember(memberId)
        }
        assertThat(exception.message).contains(ErrorCode.MEMBER_NOT_FOUND.message)
    }

    /**
     * 회원 정보를 수정합니다.
     * - given: 수정할 회원 ID, 수정할 정보 DTO, 기존 회원 객체를 설정합니다.
     * - when: `updateMember` 메소드를 호출하여 회원 정보를 수정합니다.
     * - then: 반환된 회원의 이름과 이메일이 수정한 값과 일치하는지 확인합니다.
     */
    @Test
    fun `updateMember should update and return member`() {
        // given
        val memberId = 1L
        val request = RequestUpdateMemberDto(
            memberId = memberId,
            name = "Updated Name",
            email = "updated@example.com"
        )
        val member = Member(
            userId = "user123",
            name = "User One",
            email = "user1@example.com",
            pw = "password",
            roles = listOf("USER")
        )
        whenever(memberRepository.findById(memberId)).thenReturn(Optional.of(member))

        // when
        val result = memberService.updateMember(request)

        // then
        assertThat(result.name).isEqualTo(request.name)
        assertThat(result.email).isEqualTo(request.email)
    }

    /**
     * 존재하지 않는 회원 정보 수정 시 예외가 발생하는지 테스트합니다.
     * - given: 존재하지 않는 회원 ID와 수정할 정보 DTO를 설정합니다.
     * - when: `updateMember` 메소드를 호출합니다.
     * - then: `CustomException`이 발생하고, 예외 메시지가 예상과 일치하는지 확인합니다.
     */
    @Test
    fun `updateMember should throw exception when member not found`() {
        // given
        val memberId = 1L
        val request = RequestUpdateMemberDto(
            memberId = memberId,
            name = "Updated Name"
        )
        whenever(memberRepository.findById(memberId)).thenReturn(Optional.empty())

        // when & then
        val exception = assertThrows(CustomException::class.java) {
            memberService.updateMember(request)
        }


        assertThat(exception.message).contains(ErrorCode.MEMBER_NOT_FOUND.message)
    }

    /**
     * 이메일로 회원을 조회합니다.
     * - given: 회원 이메일과 해당 이메일로 조회될 회원 객체를 설정합니다.
     * - when: `findMemberByEmail` 메소드를 호출합니다.
     * - then: 반환된 회원의 이메일이 예상과 일치하는지 확인합니다.
     */
    @Test
    fun `findMemberByEmail should return member when exists`() {
        // given
        val email = "user1@example.com"
        val member = Member(
            userId = "user123",
            name = "User One",
            email = email,
            pw = "password",
            roles = listOf("USER")
        )
        whenever(memberRepository.findByEmail(email)).thenReturn(Optional.of(member))

        // when
        val result = memberService.findMemberByEmail(email)

        // then
        assertThat(result.email).isEqualTo(email)
    }

    /**
     * 존재하지 않는 이메일로 조회 시 예외가 발생하는지 테스트합니다.
     * - given: 존재하지 않는 이메일을 설정합니다.
     * - when: `findMemberByEmail` 메소드를 호출합니다.
     * - then: `CustomException`이 발생하고, 예외 메시지가 예상과 일치하는지 확인합니다.
     */
    @Test
    fun `findMemberByEmail should throw exception when member not found`() {
        // given
        val email = "nonexistent@example.com"
        whenever(memberRepository.findByEmail(email)).thenReturn(Optional.empty())

        // when & then
        val exception = assertThrows(CustomException::class.java) {
            memberService.findMemberByEmail(email)
        }
        assertThat(exception.message).contains(ErrorCode.MEMBER_NOT_FOUND_BY_EMAIL.message)
    }

    /**
     * 사용자 ID로 회원을 조회합니다.
     * - given: 사용자 ID와 해당 ID로 조회될 회원 객체를 설정합니다.
     * - when: `findMemberByUserId` 메소드를 호출합니다.
     * - then: 반환된 회원의 사용자 ID가 예상과 일치하는지 확인합니다.
     */
    @Test
    fun `findMemberByUserId should return member and postings`() {
        // given
        val userId = "user123"
        val member = Member(
            userId = userId,
            name = "User One",
            email = "user1@example.com",
            pw = "password",
            roles = listOf("USER")
        )
        // Add postingRepository mock if needed
        whenever(memberRepository.findByUserId(userId)).thenReturn(Optional.of(member))

        // when
        val result = memberService.findMemberByUserId(userId)

        // then
        assertThat(result.userId).isEqualTo(userId)
        // Add assertions for postings if postingRepository is mocked
    }

    /**
     * Spring Security의 `loadUserByUsername` 메소드를 테스트합니다.
     * - given: 사용자 ID와 해당 ID로 조회될 회원 객체를 설정합니다.
     * - when: `loadUserByUsername` 메소드를 호출합니다.
     * - then: 반환된 `UserDetails` 객체의 username이 예상과 일치하는지 확인합니다.
     */
    @Test
    fun `loadUserByUsername should return UserDetails`() {
        // given
        val userId = "user123"
        val member = Member(
            userId = userId,
            name = "User One",
            email = "user1@example.com",
            pw = "password",
            roles = listOf("USER")
        )
        whenever(memberRepository.findByUserId(userId)).thenReturn(Optional.of(member))

        // when
        val userDetails = memberService.loadUserByUsername(userId)

        // then
        assertThat(userDetails.username).isEqualTo(userId)
    }
}
