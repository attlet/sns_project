package com.kotlin.sns.domain.Friend.service.Impl

import com.kotlin.sns.domain.Friend.const.friendApplyStatusEnum
import com.kotlin.sns.domain.Friend.dto.request.RequestCreateFriendDto
import com.kotlin.sns.domain.Friend.dto.request.RequestUpdateFriendDto
import com.kotlin.sns.domain.Friend.dto.response.ResponseFriendDto
import com.kotlin.sns.domain.Friend.entity.Friend
import com.kotlin.sns.domain.Friend.repository.friendRepository
import com.kotlin.sns.domain.Friend.service.FriendService
import com.kotlin.sns.domain.Member.repository.MemberRepository
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException

/**
 * friend 비즈니스 로직 처리
 *
 * @property friendRepository
 */
@Service
class FriendServiceImpl(
    private val friendRepository: friendRepository,
    private val memberRepository: MemberRepository
) : FriendService{
    override fun findFriendById(friendId: Long): ResponseFriendDto {
        val friend = friendRepository.findById(friendId)
            .orElseThrow { IllegalArgumentException("invalid friend id : $friendId") }

        return ResponseFriendDto(senderId = friend.sender.id, receiverId = friend.receiver.id, status = friend.status)
    }

    override fun sendFriend(requestCreateFriendDto: RequestCreateFriendDto): ResponseFriendDto {
        val senderId = requestCreateFriendDto.senderId
        val receiverId = requestCreateFriendDto.receiverId
        val status = friendApplyStatusEnum.PENDING

        val sender = memberRepository.findById(senderId)
            .orElseThrow { IllegalArgumentException("invalid member id : $senderId") }
        val receiver = memberRepository.findById(receiverId)
            .orElseThrow{ IllegalArgumentException("invalid member id : $receiverId")}

        val friend = Friend(
            sender = sender,
            receiver = receiver,
            status = status
        )

        val savedFriend = friendRepository.save(friend)

        return ResponseFriendDto(senderId = savedFriend.sender.id, receiverId = savedFriend.receiver.id, status = savedFriend.status)
    }

    override fun updateFriend(requestUpdateFriendDto: RequestUpdateFriendDto): ResponseFriendDto {
        val senderId = requestUpdateFriendDto.senderId
        val receiverId = requestUpdateFriendDto.receiverId
        val status = requestUpdateFriendDto.status

        val sender = memberRepository.findById(senderId)
            .orElseThrow { IllegalArgumentException("invalid member id : $senderId") }
        val receiver = memberRepository.findById(receiverId)
            .orElseThrow{ IllegalArgumentException("invalid member id : $receiverId")}

        val friend = Friend(
            sender = sender,
            receiver = receiver,
            status = status
        )

        val updatedFriend = friendRepository.save(friend)

        return ResponseFriendDto(senderId = updatedFriend.sender.id, receiverId = updatedFriend.receiver.id, status = status)
    }

    override fun deleteFriend(friendId: Long) {
        friendRepository.deleteById(friendId)
    }


}