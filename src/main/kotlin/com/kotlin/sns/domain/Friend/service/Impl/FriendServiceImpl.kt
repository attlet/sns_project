package com.kotlin.sns.domain.Friend.service.Impl

import com.kotlin.sns.common.exception.CustomException
import com.kotlin.sns.common.exception.ExceptionConst
import com.kotlin.sns.domain.Friend.const.friendApplyStatusEnum
import com.kotlin.sns.domain.Friend.dto.request.RequestCreateFriendDto
import com.kotlin.sns.domain.Friend.dto.request.RequestUpdateFriendDto
import com.kotlin.sns.domain.Friend.dto.response.ResponseFriendDto
import com.kotlin.sns.domain.Friend.entity.Friend
import com.kotlin.sns.domain.Friend.repository.friendRepository
import com.kotlin.sns.domain.Friend.service.FriendService
import com.kotlin.sns.domain.Member.repository.MemberRepository
import com.kotlin.sns.domain.Notification.dto.request.RequestCreateNotificationDto
import com.kotlin.sns.domain.Notification.entity.NotificationType
import com.kotlin.sns.domain.Notification.service.NotificationService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

/**
 * friend 비즈니스 로직 처리
 *
 * @property friendRepository
 */
@Service
class FriendServiceImpl(
    private val friendRepository: friendRepository,
    private val memberRepository: MemberRepository,
    private val notificationService: NotificationService
) : FriendService {

    override fun findFriendById(friendId: Long): ResponseFriendDto {
        val friend = friendRepository.findById(friendId)
            .orElseThrow {
                CustomException(
                    ExceptionConst.MEMBER,
                    HttpStatus.NOT_FOUND,
                    "Friend with id $friendId not found"
                )
            }

        return ResponseFriendDto(
            senderId = friend.sender.id,
            receiverId = friend.receiver.id,
            status = friend.status
        )
    }

    override fun sendFriend(requestCreateFriendDto: RequestCreateFriendDto): ResponseFriendDto {
        val senderId = requestCreateFriendDto.senderId
        val receiverId = requestCreateFriendDto.receiverId
        val status = friendApplyStatusEnum.PENDING

        val sender = memberRepository.findById(senderId)
            .orElseThrow {
                CustomException(
                    ExceptionConst.MEMBER,
                    HttpStatus.NOT_FOUND,
                    "Member with id $senderId not found"
                )
            }
        val receiver = memberRepository.findById(receiverId)
            .orElseThrow {
                CustomException(
                    ExceptionConst.MEMBER,
                    HttpStatus.NOT_FOUND,
                    "Member with id $receiverId not found"
                )
            }

        val friend = Friend(
            sender = sender,
            receiver = receiver,
            status = status
        )

        val savedFriend = friendRepository.save(friend)

        //친구 요청 알림 생성
        notificationService.createNotification(
            requestCreateNotificationDto = RequestCreateNotificationDto(
                receiverId = listOf(receiver.id),
                senderId = sender.id,
                type = NotificationType.FRIEND_REQUEST,
                message = "${sender.name} has sent you a friend request."
            )
        )

        return ResponseFriendDto(
            senderId = savedFriend.sender.id,
            receiverId = savedFriend.receiver.id,
            status = savedFriend.status
        )
    }

    override fun updateFriend(requestUpdateFriendDto: RequestUpdateFriendDto): ResponseFriendDto {
        val senderId = requestUpdateFriendDto.senderId
        val receiverId = requestUpdateFriendDto.receiverId
        val status = requestUpdateFriendDto.status

        val sender = memberRepository.findById(senderId)
            .orElseThrow {
                CustomException(
                    ExceptionConst.MEMBER,
                    HttpStatus.NOT_FOUND,
                    "Member with id $senderId not found"
                )
            }
        val receiver = memberRepository.findById(receiverId)
            .orElseThrow {
                CustomException(
                    ExceptionConst.MEMBER,
                    HttpStatus.NOT_FOUND,
                    "Member with id $receiverId not found"
                )
            }

        val friend = Friend(
            sender = sender,
            receiver = receiver,
            status = status
        )

        val updatedFriend = friendRepository.save(friend)

        return ResponseFriendDto(
            senderId = updatedFriend.sender.id,
            receiverId = updatedFriend.receiver.id,
            status = status
        )
    }

    override fun deleteFriend(friendId: Long) {
        if (!friendRepository.existsById(friendId)) {
            throw CustomException(
                ExceptionConst.MEMBER,
                HttpStatus.NOT_FOUND,
                "Friend with id $friendId not found"
            )
        }
        friendRepository.deleteById(friendId)
    }
}
