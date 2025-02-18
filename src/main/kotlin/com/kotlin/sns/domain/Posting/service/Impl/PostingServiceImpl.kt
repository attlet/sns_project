package com.kotlin.sns.domain.Posting.service.Impl

import com.kotlin.sns.common.exception.CustomException
import com.kotlin.sns.common.exception.ExceptionConst
import com.kotlin.sns.common.security.JwtUtil
import com.kotlin.sns.domain.Friend.const.FriendApplyStatusEnum
import com.kotlin.sns.domain.Hashtag.entity.Hashtag
import com.kotlin.sns.domain.Hashtag.repository.HashtagRepository
import com.kotlin.sns.domain.Image.entity.Image
import com.kotlin.sns.domain.Image.entity.ImageType
import com.kotlin.sns.domain.Image.service.FileStorageService
import com.kotlin.sns.domain.Image.service.ImageService
import com.kotlin.sns.domain.member.entity.Member
import com.kotlin.sns.domain.member.repository.MemberRepository
import com.kotlin.sns.domain.Notification.dto.request.RequestCreateNotificationDto
import com.kotlin.sns.domain.Notification.entity.NotificationType
import com.kotlin.sns.domain.Notification.service.NotificationService
import com.kotlin.sns.domain.Posting.dto.request.RequestCreatePostingDto
import com.kotlin.sns.domain.Posting.dto.request.RequestSearchPostingDto
import com.kotlin.sns.domain.Posting.dto.request.RequestUpdatePostingDto
import com.kotlin.sns.domain.Posting.dto.response.ResponsePostingDto
import com.kotlin.sns.domain.Posting.entity.Posting
import com.kotlin.sns.domain.Posting.mapper.PostingMapper
import com.kotlin.sns.domain.Posting.repository.PostingRepository
import com.kotlin.sns.domain.Posting.service.PostingService
import com.kotlin.sns.domain.PostingHashtag.entity.PostingHashtag
import com.kotlin.sns.domain.PostingHashtag.repository.PostingHashtagRepository
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
    private val hashtagRepository: HashtagRepository,
    private val postingHashtagRepository: PostingHashtagRepository,
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

        return createResponsePostingDto(posting)
    }

    /**
     * posting 페이징을 통해 리스트 반환
     *
     * @param pageable
     * @return
     */
    @Transactional(readOnly = true)
    override fun findPostingList(pageable: Pageable, requestSearchPostingDto: RequestSearchPostingDto): List<ResponsePostingDto> {

        logger.debug { "findPostingList request : $requestSearchPostingDto" }

        val postingList = postingRepository.findPostingList(pageable, requestSearchPostingDto)
        val responsePostingList = postingList.map { posting ->
            createResponsePostingDto(posting)
        }

        logger.debug { "response : $responsePostingList" }

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

        logger.debug { "requestCreatePostingDto : $requestCreatePostingDto" }

        val writerId = requestCreatePostingDto.writerId

        //1. 포스팅 작성자 조회
        val writer = memberRepository.findById(writerId)
            .orElseThrow {
                CustomException(
                    ExceptionConst.MEMBER,
                    HttpStatus.NOT_FOUND,
                    "writer with id $writerId not found"
                )
            }

        //2. 입력한 포스팅 내용 저장
        val savedPosting = savePosting(requestCreatePostingDto, writer)

        //3. 첨부 이미지 존재하면, 이미지 업로드
        val imageUrlList = requestCreatePostingDto.imageUrl?.let { uploadProfileImage(it, savedPosting) }

        //4. 해시태그 존재하면, 해시태그 관계 저장
        val hashTagList = requestCreatePostingDto.hashTagList?.let { saveHashTag(it, savedPosting) }

        //5. 친구가 있다면, 알림 발송
        notifyForNewPosting(writerId)
        
        logger.debug { "imageUrlList : $imageUrlList" }

        return ResponsePostingDto(
            postingId = savedPosting.id,
            writerId = writerId,
            writerName = writer.name,
            content = savedPosting.content,
            imageUrl = imageUrlList,
            hashTagList = hashTagList
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
        val newImageUrlList = requestUpdatePostingDto.imageUrl?.let { uploadProfileImage(it, posting) }

        //6. 기존 게시글의 해시태그 연결 삭제 후, 새로운 해시태그들 연결
        postingHashtagRepository.deleteByPostingId(postingId)
        val newHashtagList = requestUpdatePostingDto.hashTagList?.let { saveHashTag(it, posting) }

        return ResponsePostingDto(
            postingId = posting.id,
            writerId = posting.member.id,
            writerName = posting.member.name,
            content = posting.content,
            imageUrl = newImageUrlList,
            hashTagList = newHashtagList
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

        logger.info { "savedPosting complete" }
        return postingRepository.save(posting)
    }

    /**
     * 이미지 업로드, 엔티티 저장 메서드
     *
     * @param requestCreatePostingDto
     * @param savedPosting
     * @return
     */
    private fun uploadProfileImage(imageUrl : List<MultipartFile>, savedPosting: Posting) : List<String>{
        val uploadedImages = fileStorageService.uploadPostingImageList(imageUrl)

        val imageUrlList = mutableListOf<String>()
        val imageEntities = uploadedImages.map { url ->
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

        logger.info { "uploadProfileImage complete" }

        return imageUrlList
    }

    /**
     * 게시글 생성, 수정 시 해시태그 저장 함수
     *
     * @param hashTagList
     * @param posting
     * @return
     */
    private fun saveHashTag(hashTagList : List<String>, posting : Posting) : List<String>{
        //1.해시태그 리스트 내 중복된 태그 제거
        val hashTagList = hashTagList.distinct()

        //2. 입력된 태그들 중 기존에 db에 존재하는 해시태그만 반환
        val existHashTagList = hashtagRepository.findByTagNameForExist(hashTagList)
        val existHashTagNames = existHashTagList.map { tag -> tag.tagName }

        //3. db에 없는 해시태그들 추출
        val newHashTagNames = hashTagList.filter { it !in existHashTagNames }
        val newHashTagList = newHashTagNames.map { tagName -> Hashtag(tagName = tagName) }

        //4. 새로운 해시태그들 db에 저장
        hashtagRepository.saveAll(newHashTagList)

        //5. 게시글에 등록된 모든 해시태그들을 모아서 관계 생성
        val allHashTagList = existHashTagList + newHashTagList
        val postingHashtagList = allHashTagList.map {
            hashtag -> PostingHashtag(posting = posting, hashtag = hashtag)
        }

        //6. 생성한 관계들을 저장
        postingHashtagRepository.saveAll(postingHashtagList)

        //7. posting 엔티티에 연관관계 저장
        posting.postingHashtag.clear()
        posting.postingHashtag.addAll(postingHashtagList)

        logger.info { "saveHashTag complete" }
        return allHashTagList.map { tag -> tag.tagName }
    }

    /**
     * 새 포스팅 등록 알림 메서드
     *
     * @param writerId
     */
    private fun notifyForNewPosting(writerId : Long){
        val friends = memberRepository.findFriendsId(writerId, FriendApplyStatusEnum.ACCEPT)

        notificationService.createNotification(
            RequestCreateNotificationDto(
                receiverId = friends,
                senderId = writerId,
                type = NotificationType.NEW_POST,
                message = "친구가 포스팅을 게시했습니다."
            )
        )

    }

    private fun createResponsePostingDto(posting : Posting) : ResponsePostingDto{
        val imageUrlList = posting.imageInPosting?.map { url -> url.imageUrl }?.distinct()
        val hashtagList = posting.postingHashtag?.map { postingHashtag -> postingHashtag.hashtag.tagName }?.distinct()
        return ResponsePostingDto(
            postingId = posting.id,
            writerId = posting.member.id,
            writerName = posting.member.name,
            content = posting.content,
            imageUrl = imageUrlList,
            hashTagList = hashtagList
        )
    }




}
