package com.kotlin.sns.domain.Posting.service.Impl

import com.kotlin.sns.common.exception.CustomException
import com.kotlin.sns.common.exception.ExceptionConst
import com.kotlin.sns.common.security.JwtUtil
import com.kotlin.sns.domain.Comment.dto.response.ResponseCommentDto
import com.kotlin.sns.domain.Friend.service.FriendService
import com.kotlin.sns.domain.Image.entity.Image
import com.kotlin.sns.domain.Image.entity.ImageType
import com.kotlin.sns.domain.Image.service.ImageService
import com.kotlin.sns.domain.Member.entity.Member
import com.kotlin.sns.domain.Member.repository.MemberRepository
import com.kotlin.sns.domain.Notification.dto.request.RequestCreateNotificationDto
import com.kotlin.sns.domain.Notification.entity.NotificationType
import com.kotlin.sns.domain.Notification.service.NotificationService
import com.kotlin.sns.domain.Posting.dto.request.RequestCreatePostingDto
import com.kotlin.sns.domain.Posting.dto.request.RequestUpdatePostingDto
import com.kotlin.sns.domain.Posting.dto.response.ResponsePostingDto
import com.kotlin.sns.domain.Posting.entity.Posting
import com.kotlin.sns.domain.Posting.mapper.PostingMapper
import com.kotlin.sns.domain.Posting.repository.PostingRepository
import com.kotlin.sns.domain.Posting.service.PostingService
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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
    private val notificationService: NotificationService,
    private val imageService: ImageService,
    private val jwtUtil: JwtUtil
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
        val postingList = postingRepository.getPostingListWithComment(pageable)
        val responsePostingList = mutableListOf<ResponsePostingDto>()

        for (posting in postingList) {
            val responseCommentList = posting.comment
                .map {it ->
                    ResponseCommentDto(
                        writerId = it.member.id,
                        writerName = it.member.name,
                        content = it.content,
                        createDt = it.createdDt,
                        updateDt = it.updateDt
                    )
                }
                .toList()

            responsePostingList.add(ResponsePostingDto(
                writerId = posting.member.id,
                writerName = posting.member.name,
                content = posting.content,
                commentList = responseCommentList
            ))
        }

        return responsePostingList
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

        val savedPosting = savePosting(requestCreatePostingDto, writer)
        val imageEntities = uploadProfileImage(requestCreatePostingDto, savedPosting)
        notifyForNewPosting(writerId)

        return postingMapper.toDto(savedPosting)
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

        jwtUtil.checkPermission(postingId)

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
        jwtUtil.checkPermission(postingId)
        postingRepository.deleteById(postingId)
    }



    /**
     * posting 엔티티 저장 메서드
     *
     * @param requestCreatePostingDto
     * @param writer
     * @return
     */
    private fun savePosting(requestCreatePostingDto: RequestCreatePostingDto, writer: Member) : Posting {
        val posting = postingMapper.toEntity(requestCreatePostingDto, writer)
        return postingRepository.save(posting)
    }

    /**
     * 이미지 업로드, 엔티티 저장 메서드
     *
     * @param requestCreatePostingDto
     * @param savedPosting
     * @return
     */
    private fun uploadProfileImage(requestCreatePostingDto: RequestCreatePostingDto, savedPosting: Posting) : List<Image>{
        val uploadedImages = imageService.uploadPostingImageList(requestCreatePostingDto.imageUrl)

        if(uploadedImages != null){

            val imageEntities = uploadedImages.map {
                    url -> Image(
                imageUrl = url,
                imageType = ImageType.IN_POSTING,
                posting = savedPosting
            )
            }

            imageService.createImage(imageEntities)
            savedPosting.imageInPosting?.addAll(imageEntities)   //imageInPosting이 null이면 addAll 호출하지 않도록 함
            postingRepository.save(savedPosting)
            return imageEntities
        }

        return emptyList()
    }

    /**
     * 새 포스팅 등록 알림 메서드
     *
     * @param writerId
     */
    private fun notifyForNewPosting(writerId : Long){
        val friends = memberRepository.findFriendsId(writerId)

        notificationService.createNotification(
            RequestCreateNotificationDto(
                receiverId = friends,
                senderId = writerId,
                type = NotificationType.NEW_POST,
                message = "친구가 포스팅을 게시했습니다."
            )
        )

    }




}
