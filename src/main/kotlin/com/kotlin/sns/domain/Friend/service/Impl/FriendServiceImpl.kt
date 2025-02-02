package com.kotlin.sns.domain.Friend.service.Impl

import com.kotlin.sns.common.exception.CustomException
import com.kotlin.sns.common.exception.ExceptionConst
import com.kotlin.sns.domain.Friend.const.FriendApplyStatusEnum
import com.kotlin.sns.domain.Friend.dto.request.RequestCreateFriendDto
import com.kotlin.sns.domain.Friend.dto.request.RequestUpdateFriendDto
import com.kotlin.sns.domain.Friend.dto.response.ResponseFriendDto
import com.kotlin.sns.domain.Friend.entity.Friend
import com.kotlin.sns.domain.Friend.repository.friendRepository
import com.kotlin.sns.domain.Friend.service.FriendService
import com.kotlin.sns.domain.Member.entity.Member
import com.kotlin.sns.domain.Member.repository.MemberRepository
import com.kotlin.sns.domain.Notification.dto.request.RequestCreateNotificationDto
import com.kotlin.sns.domain.Notification.entity.NotificationType
import com.kotlin.sns.domain.Notification.service.NotificationService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

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

    /**
     * friend id 기반으로 하나 조회
     *
     * @param friendId
     * @return
     */
    @Transactional(readOnly = true)
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
            friendRequestId = friend.id,
            senderId = friend.sender.id,
            receiverId = friend.receiver.id,
            status = friend.status
        )
    }

    /**
     * 친구 요청 보낼 때 로직
     *
     * @param requestCreateFriendDto
     * @return
     */
    @Transactional
    override fun sendFriend(requestCreateFriendDto: RequestCreateFriendDto): ResponseFriendDto {
        val senderId = requestCreateFriendDto.senderId
        val receiverId = requestCreateFriendDto.receiverId
        val status = FriendApplyStatusEnum.PENDING

        //만약 이미 친구 요청을 보낸 상대라면 exception 반환
        if(friendRepository.isFriendRequestExist(receiverId, senderId)){
            throw CustomException(
                ExceptionConst.FRIEND,
                HttpStatus.CONFLICT,
                "Already send friend request to $senderId")
        }

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
        notifyForFriendRequest(sender, receiver)

        return ResponseFriendDto(
            friendRequestId = savedFriend.id,
            senderId = savedFriend.sender.id,
            receiverId = savedFriend.receiver.id,
            status = savedFriend.status
        )
    }

    /**
     * 친구 요청에 대해 응답에 대한 처리
     *
     * ACCEPT : 수락
     * BLOCKED : 거절
     *
     * @param requestUpdateFriendDto
     * @return
     */
    @Transactional
    override fun updateFriend(requestUpdateFriendDto: RequestUpdateFriendDto): ResponseFriendDto {
        val friendRequestId = requestUpdateFriendDto.friendRequestId
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

        val friendRequest = friendRepository.findById(friendRequestId)
            .orElseThrow{
                CustomException(
                    ExceptionConst.FRIEND,
                    HttpStatus.NOT_FOUND,
                    "Friend Request id $friendRequestId not found"
                )
            }

        friendRequest.status = status

        return ResponseFriendDto(
            friendRequestId = friendRequest.id,
            senderId = friendRequest.sender.id,
            receiverId = friendRequest.receiver.id,
            status = status
        )
    }
    @Transactional
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

    /**
     * 친구 요청 시 상대에게 알림보내는 로직
     *
     * @param sender
     * @param receiver
     */
    private fun notifyForFriendRequest(sender : Member, receiver: Member){

        notificationService.createNotification(
            RequestCreateNotificationDto(
                receiverId = listOf(receiver.id),
                senderId = sender.id,
                type = NotificationType.FRIEND_REQUEST,
                message = "${sender.name}으로부터 친구 요청이 도착했습니다."
            )
        )

    }
}
