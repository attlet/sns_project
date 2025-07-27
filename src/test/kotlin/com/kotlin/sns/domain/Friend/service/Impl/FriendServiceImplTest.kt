
package com.kotlin.sns.domain.Friend.service

import com.kotlin.sns.common.exception.CustomException
import com.kotlin.sns.common.exception.ErrorCode
import com.kotlin.sns.domain.Friend.const.FriendApplyStatusEnum
import com.kotlin.sns.domain.Friend.dto.request.RequestCreateFriendDto
import com.kotlin.sns.domain.Friend.dto.request.RequestUpdateFriendDto
import com.kotlin.sns.domain.Friend.entity.Friend
import com.kotlin.sns.domain.Friend.repository.friendRepository
import com.kotlin.sns.domain.Friend.service.Impl.FriendServiceImpl
import com.kotlin.sns.domain.Member.entity.Member
import com.kotlin.sns.domain.Member.repository.MemberRepository
import com.kotlin.sns.domain.Notification.service.NotificationService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.whenever
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import java.util.*

@SpringBootTest
class FriendServiceImplTest {

    @MockBean
    private lateinit var friendRepository: friendRepository

    @MockBean
    private lateinit var memberRepository: MemberRepository

    @MockBean
    private lateinit var notificationService: NotificationService

    @Autowired
    private lateinit var friendService: FriendServiceImpl

    /**
     * 친구 요청을 보냅니다.
     * - given: 친구 요청을 보내는 사용자와 받는 사용자 정보를 설정합니다.
     * - when: `sendFriend` 메소드를 호출하여 친구 요청을 보냅니다.
     * - then: 반환된 친구 요청의 상태가 `PENDING`이고, 보낸 사람과 받는 사람이 일치하는지 확인합니다.
     */
    @Test
    @DisplayName("친구 요청 성공")
    fun sendFriend_Success() {
        // given
        val senderId = 1L
        val receiverId = 2L
        val sender = Member("senderId", "sender", "sender@test.com", "password", roles = listOf("USER"))
        val receiver = Member("receiverId", "receiver", "receiver@test.com", "password", roles = listOf("USER"))
        sender.id = senderId
        receiver.id = receiverId

        val request = RequestCreateFriendDto(sender.id!!, receiver.id!!)

        whenever(memberRepository.findById(sender.id!!)).thenReturn(Optional.of(sender))
        whenever(memberRepository.findById(receiver.id!!)).thenReturn(Optional.of(receiver))
        whenever(friendRepository.isFriendRequestExist(receiver.id!!, sender.id!!)).thenReturn(false)
        whenever(friendRepository.save(any())).thenAnswer { it.arguments[0] as Friend }
        doNothing().whenever(notificationService).createNotification(Mockito.any())

        // when
        val result = friendService.sendFriend(request)

        // then
        assertNotNull(result)
        assertEquals(sender.id, result.senderId)
        assertEquals(receiver.id, result.receiverId)
        assertEquals(FriendApplyStatusEnum.PENDING, result.status)
    }

    /**
     * 이미 친구 요청을 보낸 경우 예외가 발생하는지 테스트합니다.
     * - given: 이미 친구 요청이 존재하는 상황을 설정합니다.
     * - when: `sendFriend` 메소드를 호출합니다.
     * - then: `CustomException`이 발생하고, 예외 코드가 `ALREADY_FRIENDS`인지 확인합니다.
     */
    @Test
    @DisplayName("친구 요청 실패 - 이미 친구 요청을 보낸 경우")
    fun sendFriend_Fail_AlreadyFriends() {
        // given
        val senderId = 1L
        val receiverId = 2L
        val request = RequestCreateFriendDto(senderId, receiverId)

        whenever(friendRepository.isFriendRequestExist(receiverId, senderId)).thenReturn(true)

        // when & then
        val exception = assertThrows(CustomException::class.java) {
            friendService.sendFriend(request)
        }
        assertEquals(ErrorCode.ALREADY_FRIENDS, exception.errorCode)
    }

    /**
     * 친구 요청을 수락/거절합니다.
     * - given: 친구 요청을 수락 또는 거절하는 상황을 설정합니다.
     * - when: `updateFriend` 메소드를 호출하여 친구 요청 상태를 변경합니다.
     * - then: 반환된 친구 요청의 상태가 변경된 상태와 일치하는지 확인합니다.
     */
    @Test
    @DisplayName("친구 요청 수락/거절 성공")
    fun updateFriend_Success() {
        // given
        val sender = Member("senderId", "sender", "sender@test.com", "password", roles = listOf("USER"))
        sender.id = 1L
        val receiver = Member("receiverId", "receiver", "receiver@test.com", "password", roles = listOf("USER"))
        receiver.id = 2L
        val friend = Friend(sender, receiver, FriendApplyStatusEnum.PENDING)
        friend.id = 1L
        val request = RequestUpdateFriendDto(friend.id!!, sender.id!!, receiver.id!!, FriendApplyStatusEnum.ACCEPT)

        whenever(memberRepository.findById(sender.id!!)).thenReturn(Optional.of(sender))
        whenever(memberRepository.findById(receiver.id!!)).thenReturn(Optional.of(receiver))
        whenever(friendRepository.findById(friend.id!!)).thenReturn(Optional.of(friend))
        doNothing().whenever(notificationService).createNotification(any())

        // when
        val result = friendService.updateFriend(request)

        // then
        assertNotNull(result)
        assertEquals(FriendApplyStatusEnum.ACCEPT, result.status)
    }

    /**
     * 친구를 삭제합니다.
     * - given: 삭제할 친구 관계가 존재하는 상황을 설정합니다.
     * - when: `deleteFriend` 메소드를 호출하여 친구 관계를 삭제합니다.
     * - then: 예외가 발생하지 않는지 확인합니다.
     */
    @Test
    @DisplayName("친구 삭제 성공")
    fun deleteFriend_Success() {
        // given
        val friendId = 1L
        whenever(friendRepository.existsById(friendId)).thenReturn(true)
        doNothing().whenever(friendRepository).deleteById(friendId)

        // when & then
        assertDoesNotThrow {
            friendService.deleteFriend(friendId)
        }
    }

    /**
     * 존재하지 않는 친구 관계를 삭제 시 예외가 발생하는지 테스트합니다.
     * - given: 삭제할 친구 관계가 존재하지 않는 상황을 설정합니다.
     * - when: `deleteFriend` 메소드를 호출합니다.
     * - then: `CustomException`이 발생하고, 예외 코드가 `FRIEND_REQUEST_NOT_FOUND`인지 확인합니다.
     */
    @Test
    @DisplayName("친구 삭제 실패 - 존재하지 않는 친구 요청")
    fun deleteFriend_Fail_NotFound() {
        // given
        val friendId = 1L
        whenever(friendRepository.existsById(friendId)).thenReturn(false)

        // when & then
        val exception = assertThrows(CustomException::class.java) {
            friendService.deleteFriend(friendId)
        }
        assertEquals(ErrorCode.FRIEND_REQUEST_NOT_FOUND, exception.errorCode)
    }
}
