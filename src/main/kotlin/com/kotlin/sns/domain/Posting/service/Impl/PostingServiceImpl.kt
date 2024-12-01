package com.kotlin.sns.domain.Posting.service.Impl

import com.kotlin.sns.common.exception.CustomException
import com.kotlin.sns.common.exception.ExceptionConst
import com.kotlin.sns.common.security.JwtUtil
import com.kotlin.sns.domain.Comment.dto.response.ResponseCommentDto
import com.kotlin.sns.domain.Image.entity.Image
import com.kotlin.sns.domain.Image.entity.ImageType
import com.kotlin.sns.domain.Image.service.FileStorageService
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
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

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
    private val fileStorageService: FileStorageService,
    private val imageService: ImageService,
    private val jwtUtil: JwtUtil
) : PostingService {

    private val logger = KotlinLogging.logger{}

    /**
     * uuid 기반으로 posting 반환
     *
     * @param postingId
     * @return
     */
    @Transactional(readOnly = true)
    override fun findPostingById(postingId: Long): ResponsePostingDto {
        val posting = postingRepository.findByIdForDetail(postingId)
            .orElseThrow { CustomException(
                ExceptionConst.POSTING,
                HttpStatus.NOT_FOUND,
                "Posting with id $postingId not found"
            ) }

        val imageUrlList = posting.imageInPosting.map { url -> url.imageUrl }

        val responseCommentDtoList = posting.comment.map {
            comment ->
                ResponseCommentDto(
                    writerId = comment.member.id,
                    writerName = comment.member.name,
                    content = comment.content,
                    createDt = comment.createdDt,
                    updateDt = comment.updateDt
                )
        }

        return createResponsePostingDto(posting, imageUrlList, responseCommentDtoList)
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
                ?.map {comment ->
                    ResponseCommentDto(
                        writerId = comment.member.id,
                        writerName = comment.member.name,
                        content = comment.content,
                        createDt = comment.createdDt,
                        updateDt = comment.updateDt
                    )
                }
                ?.toList()

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
        val imageUrlList = uploadProfileImage(requestCreatePostingDto.imageUrl, savedPosting)
        notifyForNewPosting(writerId)

        logger.debug { "imageUrlList : $imageUrlList" }
        return ResponsePostingDto(
            writerId = writerId,
            writerName = writer.name,
            content = savedPosting.content,
            commentList = null,
            imageUrl = imageUrlList
        )
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

        // 1. 수정할 posting 조회
        val posting = postingRepository.findById(postingId)
            .orElseThrow {
                CustomException(
                    ExceptionConst.POSTING,
                    HttpStatus.NOT_FOUND,
                    "Posting with id $postingId not found"
                )
            }

        // 2. 해당 posting 을 수정할 권한이 있는지 체크
        jwtUtil.checkPermission(postingId)

        // 3. content 내용 업데이트
        requestUpdatePostingDto.content?.let { posting.content = it }

        //4. 첨부 이미지 변경있다면, 기존 이미지 삭제
        //null 체크를 내부적으로 하기 때문에 외부에서 체크x
        imageService.deleteAllByPostingId(postingId)
        fileStorageService.deleteImagesByPostingId(postingId)
        posting.imageInPosting.clear()

        //5. 새로운 이미지 저장
        val newImageUrlList = uploadProfileImage(requestUpdatePostingDto.imageUrl, posting)

        return ResponsePostingDto(
            writerId = posting.member.id,
            writerName = posting.member.name,
            content = posting.content,
            imageUrl = newImageUrlList
        )
    }


    /**
     * posting 삭제
     *
     * @param postingId
     */
    @Transactional
    override fun deletePosting(postingId: Long) {
        val posting = postingRepository.findById(postingId)
            .orElseThrow {
                CustomException(
                    ExceptionConst.POSTING,
                    HttpStatus.NOT_FOUND,
                    "Posting with id $postingId not found"
                )
            }
        //1. 글 삭제할 권한 체크
        jwtUtil.checkPermission(posting.member.id)

        //2. 서버에 저장된 이미지 삭제
        if(!posting.imageInPosting.isNullOrEmpty()){
            logger.debug { "imageInPosting delete start" }
            fileStorageService.deleteImagesByPostingId(postingId)
        }

        //3. posting 삭제
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
        val posting = Posting(
            content = requestCreatePostingDto.content,
            member = writer
        )
//        val posting = postingMapper.toEntity(requestCreatePostingDto, writer)
        return postingRepository.save(posting)
    }

    /**
     * 이미지 업로드, 엔티티 저장 메서드
     *
     * @param requestCreatePostingDto
     * @param savedPosting
     * @return
     */
    private fun uploadProfileImage(imageUrl : List<MultipartFile>?, savedPosting: Posting) : List<String>{

        if(imageUrl != null){
            val uploadedImages = fileStorageService.uploadPostingImageList(imageUrl)

            val imageUrlList = mutableListOf<String>()
            val imageEntities = uploadedImages.map {
                    url ->
                imageUrlList.add(url)
                Image(
                    imageUrl = url,
                    imageType = ImageType.IN_POSTING,
                    posting = savedPosting
                )
            }

            imageService.createImage(imageEntities)             //image 엔티티 저장
            savedPosting.imageInPosting.addAll(imageEntities)   //posting에 image 연관관계 설정
            postingRepository.save(savedPosting)

            return imageUrlList
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

    private fun createResponsePostingDto(
        posting : Posting,
        imageUrlList: List<String>? = null,
        commentList: List<ResponseCommentDto>? = null
    ) : ResponsePostingDto{
        return ResponsePostingDto(
            writerId = posting.member.id,
            writerName = posting.member.name,
            content = posting.content,
            imageUrl = imageUrlList,
            commentList = commentList
        )
    }




}
