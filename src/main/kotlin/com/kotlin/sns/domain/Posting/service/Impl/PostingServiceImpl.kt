package com.kotlin.sns.domain.Posting.service.Impl

import com.kotlin.sns.common.exception.CustomException
import com.kotlin.sns.common.exception.ExceptionConst
import com.kotlin.sns.domain.Comment.dto.response.ResponseCommentDto
import com.kotlin.sns.domain.Friend.service.FriendService
import com.kotlin.sns.domain.Image.entity.Image
import com.kotlin.sns.domain.Image.entity.ImageType
import com.kotlin.sns.domain.Image.service.ImageService
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
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.reflect.full.memberProperties
import kotlin.streams.toList

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
    private val notificationService: NotificationService,
    private val imageService: ImageService
) : PostingService {

    /**
     * uuid 기반으로 posting 반환
     *
     * @param postingId
     * @return
     */
    @Transactional(readOnly = true)
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
     * posting 페이징을 통해 리스트 반환
     *
     * @param pageable
     * @return
     */
    override fun findPostingList(pageable: Pageable): List<ResponsePostingDto> {
        val postingList = postingRepository.getPostingList(pageable)
        val responseList = mutableListOf<ResponsePostingDto>()

        for (posting in postingList) {
            val responseCommentList = posting.comment
                .map {it ->
                    ResponseCommentDto(
                        writerId = it.member.id,
                        writerName = it.member.name,
                        content = it.content,
                        createAt = it.createdDt,
                        updateAt = it.updateDt
                    )
                }
                .toList()

            responseList.add(ResponsePostingDto(
                writerId = posting.member.id,
                writerName = posting.member.name,
                content = posting.content,
                commentList = responseCommentList
            ))
        }

        return responseList
    }

    /**
     * posting 생성
     *
     * @param requestCreatePostingDto
     * @return
     */
    @Transactional
    override fun createPosting(requestCreatePostingDto: RequestCreatePostingDto): ResponsePostingDto {
        val writerId = requestCreatePostingDto.writerId

        val writer = memberRepository.findById(writerId)
            .orElseThrow {
                CustomException(
                    ExceptionConst.MEMBER,
                    HttpStatus.NOT_FOUND,
                    "writer with id $writerId not found"
                )
            }

        val posting = postingMapper.toEntity(requestCreatePostingDto, writer)
        val savedPosting = postingRepository.save(posting)

        //포스팅에 첨부된 이미지 업로드 후, 결과 이미지 url 반환
        val uploadedImages = imageService.uploadPostingImageList(requestCreatePostingDto.imageUrl)

        if(uploadedImages != null){
            val images =uploadedImages.map {
                url -> Image(
                    imageUrl = url,
                    imageType = ImageType.IN_POSTING,
                    posting = savedPosting
                )
            }

            imageService.createImage(images)
            savedPosting.imageInPosting?.addAll(images)   //imageInPosting이 null이면 addAll 호출하지 않도록 함
            postingRepository.save(savedPosting)
        }

        notificationService.createNotification(
            RequestCreateNotificationDto(
                receiverId = memberRepository.findFriendsId(writerId),
                senderId = writerId,
                type = NotificationType.NEW_POST,
                message = "친구가 포스팅을 게시했습니다."
             )
        )

        return postingMapper.toDto(posting)
    }

    /**
     * uuid로 posting 찾아서 update
     *
     * @param requestUpdatePostingDto
     * @return
     */
    @Transactional
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
                    //이미지 수정 추가 필요
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
    @Transactional
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
}
