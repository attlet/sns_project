package com.kotlin.sns.domain.Posting.service.Impl

import com.kotlin.sns.common.exception.CustomException
import com.kotlin.sns.common.exception.ExceptionConst
import com.kotlin.sns.domain.Friend.service.FriendService
import com.kotlin.sns.domain.Member.repository.MemberRepository
import com.kotlin.sns.domain.Notification.dto.request.RequestCreateNotificationDto
import com.kotlin.sns.domain.Notification.entity.NotificationType
import com.kotlin.sns.domain.Notification.service.NotificationService
import com.kotlin.sns.domain.Posting.dto.request.RequestCreatePostingDto
import com.kotlin.sns.domain.Posting.dto.request.RequestUpdatePostingDto
import com.kotlin.sns.domain.Posting.dto.response.ResponsePostingDto
import com.kotlin.sns.domain.Posting.mapper.PostingMapper
import com.kotlin.sns.domain.Posting.repository.PostingRepository
import com.kotlin.sns.domain.Posting.service.PostingService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import kotlin.reflect.full.memberProperties

/**
 * posting 관련 비즈니스 로직
 *
 * @property postingRepository
 * @property postingMapper
 */
@Service
class PostingServiceImpl(
    private val postingRepository: PostingRepository,
    private val memberRepository: MemberRepository,
    private val postingMapper: PostingMapper,
    private val friendService: FriendService,
    private val notificationService: NotificationService
) : PostingService {

    /**
     * uuid 기반으로 posting 반환
     *
     * @param postingId
     * @return
     */
    override fun findPostingById(postingId: Long): ResponsePostingDto {
        val posting = postingRepository.findById(postingId)
            .orElseThrow {
                CustomException(
                    ExceptionConst.POSTING,
                    HttpStatus.NOT_FOUND,
                    "Posting with id $postingId not found"
                )
            }

        return postingMapper.toDto(posting)
    }

    /**
     * posting 생성
     *
     * @param requestCreaetePostingDto
     * @return
     */
    override fun createPosting(requestCreatePostingDto: RequestCreatePostingDto): ResponsePostingDto {

        val savedPosting = postingMapper.toEntity(requestCreatePostingDto)
        val posting = postingRepository.save(savedPosting)

        notifyToFriend(requestCreatePostingDto)  //친구들에게 알림 전송

        return postingMapper.toDto(posting)
    }

    /**
     * uuid로 posting 찾아서 update
     *
     * @param requestUpdatePostingDto
     * @return
     */
    override fun updatePosting(requestUpdatePostingDto: RequestUpdatePostingDto): ResponsePostingDto {
        val postingId = requestUpdatePostingDto.postingId
        val posting = postingRepository.findById(postingId)
            .orElseThrow {
                CustomException(
                    ExceptionConst.POSTING,
                    HttpStatus.NOT_FOUND,
                    "Posting with id $postingId not found"
                )
            }

        val fields = requestUpdatePostingDto::class.memberProperties

        for (field in fields) {
            val fieldName = field.name  // 필드의 이름
            val fieldValue = field.getter.call(requestUpdatePostingDto)  // 필드의 값

            if (fieldValue != null) {
                when (fieldName) {
                    "content" -> posting.content = fieldValue as String
                    "imageUrl" -> posting.imageUrl = fieldValue as String?
                }
            }
        }

        return postingMapper.toDto(posting)
    }

    /**
     * posting 삭제
     *
     * @param postingId
     */
    override fun deletePosting(postingId: Long) {
        if (!postingRepository.existsById(postingId)) {
            throw CustomException(
                ExceptionConst.POSTING,
                HttpStatus.NOT_FOUND,
                "Posting with id $postingId not found"
            )
        }
        postingRepository.deleteById(postingId)
    }

    /**
     * 친구에게 알림 보내는 로직 처리하는 함수를 따로 분리해서 작성
     *
     * @param requestCreatePostingDto
     */
    fun notifyToFriend(requestCreatePostingDto: RequestCreatePostingDto){
        val senderId = requestCreatePostingDto.writerId
        val receiversId = memberRepository.findFriendsId(senderId)

        notificationService.createNotification(
            RequestCreateNotificationDto(
                receiverId = receiversId,
                senderId = senderId,
                type = NotificationType.NEW_POST,
                message = "친구가 포스팅을 게시했습니다."
            )
        )

    }
}
