package com.kotlin.sns.domain.Posting

import com.kotlin.sns.common.exception.CustomException
import com.kotlin.sns.common.exception.ErrorCode
import com.kotlin.sns.common.security.JwtUtil
import com.kotlin.sns.domain.Friend.const.FriendApplyStatusEnum
import com.kotlin.sns.domain.Hashtag.entity.Hashtag
import com.kotlin.sns.domain.Hashtag.repository.HashtagRepository
import com.kotlin.sns.domain.Image.entity.Image
import com.kotlin.sns.domain.Image.entity.ImageType
import com.kotlin.sns.domain.Image.service.FileStorageService
import com.kotlin.sns.domain.Image.service.ImageService
import com.kotlin.sns.domain.Member.entity.Member
import com.kotlin.sns.domain.Member.repository.MemberRepository
import com.kotlin.sns.domain.Notification.dto.request.RequestCreateNotificationDto
import com.kotlin.sns.domain.Notification.service.NotificationService
import com.kotlin.sns.domain.Posting.dto.request.RequestCreatePostingDto
import com.kotlin.sns.domain.Posting.dto.request.RequestSearchPostingDto
import com.kotlin.sns.domain.Posting.dto.request.RequestUpdatePostingDto
import com.kotlin.sns.domain.Posting.dto.response.ResponsePostingDto
import com.kotlin.sns.domain.Posting.entity.Posting
import com.kotlin.sns.domain.Posting.repository.PostingRepository
import com.kotlin.sns.domain.Posting.service.Impl.PostingServiceImpl
import com.kotlin.sns.domain.PostingHashtag.entity.PostingHashtag
import com.kotlin.sns.domain.PostingHashtag.repository.PostingHashtagRepository
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageRequest
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@ExtendWith(MockKExtension::class)
class PostingServiceTest {

    @MockK
    private lateinit var postingRepository: PostingRepository

    @MockK
    private lateinit var memberRepository: MemberRepository

    @MockK
    private lateinit var hashtagRepository: HashtagRepository

    @MockK
    private lateinit var postingHashtagRepository: PostingHashtagRepository

    @MockK
    private lateinit var notificationService: NotificationService

    @MockK
    private lateinit var fileStorageService: FileStorageService

    @MockK
    private lateinit var imageService: ImageService

    @MockK
    private lateinit var jwtUtil: JwtUtil

    @InjectMockKs
    private lateinit var postingService: PostingServiceImpl

    // 테스트 데이터
    private val testMemberId = 1L
    private val testPostingId = 1L
    private val testContent = "테스트 게시글 내용"
    private val testMemberName = "테스트 사용자"
    private val testHashtags = listOf("테스트", "해시태그")
    private val testImageUrls = listOf("http://example.com/image1.jpg", "http://example.com/image2.jpg")

    @BeforeEach
    fun setUp() {
        clearAllMocks()
    }

    @Nested
    @DisplayName("게시글 조회 테스트")
    inner class FindPostingTests {

        @Test
        @DisplayName("ID로 게시글을 성공적으로 조회한다")
        fun findPostingByIdSuccess() {
            // given
            val testMember = Member(id = testMemberId, name = testMemberName, email = "test@example.com")
            val testPosting = Posting(
                id = testPostingId,
                content = testContent,
                member = testMember,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
            
            // 이미지와 해시태그 설정
            val images = testImageUrls.map { url ->
                Image(imageUrl = url, imageType = ImageType.IN_POSTING, posting = testPosting)
            }
            testPosting.imageInPosting.addAll(images)
            
            val hashtags = testHashtags.map { tagName -> Hashtag(tagName = tagName) }
            val postingHashtags = hashtags.map { hashtag -> 
                PostingHashtag(posting = testPosting, hashtag = hashtag)
            }
            testPosting.postingHashtag.addAll(postingHashtags)

            every { postingRepository.findByIdForDetail(testPostingId) } returns Optional.of(testPosting)

            // when
            val result = postingService.findPostingById(testPostingId)

            // then
            assertEquals(testPostingId, result.postingId)
            assertEquals(testMemberId, result.writerId)
            assertEquals(testMemberName, result.writerName)
            assertEquals(testContent, result.content)
            assertEquals(testImageUrls, result.imageUrl)
            assertEquals(testHashtags, result.hashTagList)
            
            verify(exactly = 1) { postingRepository.findByIdForDetail(testPostingId) }
        }

        @Test
        @DisplayName("존재하지 않는 게시글 ID로 조회 시 예외가 발생한다")
        fun findPostingByIdNotFound() {
            // given
            every { postingRepository.findByIdForDetail(testPostingId) } returns Optional.empty()

            // when & then
            val exception = assertThrows(CustomException::class.java) {
                postingService.findPostingById(testPostingId)
            }
            
            assertEquals(ErrorCode.POST_NOT_FOUND, exception.errorCode)
            verify(exactly = 1) { postingRepository.findByIdForDetail(testPostingId) }
        }

        @Test
        @DisplayName("게시글 목록을 성공적으로 조회한다")
        fun findPostingListSuccess() {
            // given
            val pageable = PageRequest.of(0, 10)
            val searchDto = RequestSearchPostingDto(keyword = "테스트")
            
            val testMember = Member(id = testMemberId, name = testMemberName, email = "test@example.com")
            val testPosting1 = Posting(
                id = 1L,
                content = "첫 번째 테스트 게시글",
                member = testMember,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
            val testPosting2 = Posting(
                id = 2L,
                content = "두 번째 테스트 게시글",
                member = testMember,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
            
            // 이미지와 해시태그 설정
            val postings = listOf(testPosting1, testPosting2)
            
            every { postingRepository.findPostingList(pageable, searchDto) } returns postings

            // when
            val result = postingService.findPostingList(pageable, searchDto)

            // then
            assertEquals(2, result.size)
            assertEquals(1L, result[0].postingId)
            assertEquals(2L, result[1].postingId)
            
            verify(exactly = 1) { postingRepository.findPostingList(pageable, searchDto) }
        }
    }
    
    @Nested
    @DisplayName("게시글 생성 테스트")
    inner class CreatePostingTests {

        @Test
        @DisplayName("게시글을 성공적으로 생성한다")
        fun createPostingSuccess() {
            // given
            val testMember = Member(id = testMemberId, name = testMemberName, email = "test@example.com")
            val createDto = RequestCreatePostingDto(
                writerId = testMemberId,
                content = testContent,
                hashTagList = testHashtags,
                imageUrl = mockk<List<MultipartFile>>()
            )
            
            val savedPosting = Posting(
                id = testPostingId,
                content = testContent,
                member = testMember
            )
            
            val friendIds = listOf(2L, 3L)
            
            // Mock 설정
            every { memberRepository.findById(testMemberId) } returns Optional.of(testMember)
            every { postingRepository.save(any()) } returns savedPosting
            every { fileStorageService.uploadPostingImageList(any()) } returns testImageUrls
            every { imageService.createImage(any()) } just runs
            every { hashtagRepository.findByTagNameForExist(testHashtags) } returns emptyList()
            every { hashtagRepository.saveAll(any<List<Hashtag>>()) } returns testHashtags.map { Hashtag(tagName = it) }
            every { postingHashtagRepository.saveAll(any<List<PostingHashtag>>()) } returns emptyList()
            every { memberRepository.findFriendsId(testMemberId, FriendApplyStatusEnum.ACCEPT) } returns friendIds
            every { notificationService.createNotification(any()) } just runs

            // when
            val result = postingService.createPosting(createDto)

            // then
            assertEquals(testPostingId, result.postingId)
            assertEquals(testMemberId, result.writerId)
            assertEquals(testMemberName, result.writerName)
            assertEquals(testContent, result.content)
            
            // 검증
            verify { memberRepository.findById(testMemberId) }
            verify { postingRepository.save(any()) }
            verify { fileStorageService.uploadPostingImageList(any()) }
            verify { imageService.createImage(any()) }
            verify { hashtagRepository.findByTagNameForExist(testHashtags) }
            verify { hashtagRepository.saveAll(any<List<Hashtag>>()) }
            verify { postingHashtagRepository.saveAll(any<List<PostingHashtag>>()) }
            verify { memberRepository.findFriendsId(testMemberId, FriendApplyStatusEnum.ACCEPT) }
            verify { notificationService.createNotification(any()) }
        }

        @Test
        @DisplayName("존재하지 않는 회원 ID로 게시글 생성 시 예외가 발생한다")
        fun createPostingWithInvalidMemberIdFails() {
            // given
            val createDto = RequestCreatePostingDto(
                writerId = testMemberId,
                content = testContent
            )
            
            every { memberRepository.findById(testMemberId) } returns Optional.empty()

            // when & then
            assertThrows(CustomException::class.java) {
                postingService.createPosting(createDto)
            }
            
            verify { memberRepository.findById(testMemberId) }
            verify(exactly = 0) { postingRepository.save(any()) }
        }
        
        @Test
        @DisplayName("이미지 업로드 실패 시 예외가 발생한다")
        fun createPostingWithImageUploadFailure() {
            // given
            val testMember = Member(id = testMemberId, name = testMemberName, email = "test@example.com")
            val createDto = RequestCreatePostingDto(
                writerId = testMemberId,
                content = testContent,
                imageUrl = mockk<List<MultipartFile>>()
            )
            
            val savedPosting = Posting(
                id = testPostingId,
                content = testContent,
                member = testMember
            )
            
            // Mock 설정
            every { memberRepository.findById(testMemberId) } returns Optional.of(testMember)
            every { postingRepository.save(any()) } returns savedPosting
            every { fileStorageService.uploadPostingImageList(any()) } throws RuntimeException("이미지 업로드 실패")

            // when & then
            assertThrows(RuntimeException::class.java) {
                postingService.createPosting(createDto)
            }
            
            verify { memberRepository.findById(testMemberId) }
            verify { postingRepository.save(any()) }
            verify { fileStorageService.uploadPostingImageList(any()) }
        }
    }
    
    @Nested
    @DisplayName("게시글 수정 테스트")
    inner class UpdatePostingTests {

        @Test
        @DisplayName("게시글을 성공적으로 수정한다")
        fun updatePostingSuccess() {
            // given
            val testMember = Member(id = testMemberId, name = testMemberName, email = "test@example.com")
            val testPosting = Posting(
                id = testPostingId,
                content = testContent,
                member = testMember
            )
            
            val updatedContent = "수정된 게시글 내용"
            val updateDto = RequestUpdatePostingDto(
                postingId = testPostingId,
                content = updatedContent,
                hashTagList = listOf("수정", "해시태그"),
                imageUrl = mockk<List<MultipartFile>>()
            )
            
            // Mock 설정
            every { postingRepository.findById(testPostingId) } returns Optional.of(testPosting)
            every { jwtUtil.checkPermission(any()) } just runs
            every { imageService.deleteAllByPostingId(testPostingId) } just runs
            every { fileStorageService.deleteImagesByPostingId(testPostingId) } just runs
            every { fileStorageService.uploadPostingImageList(any()) } returns testImageUrls
            every { postingHashtagRepository.deleteByPostingId(testPostingId) } just runs
            every { hashtagRepository.findByTagNameForExist(any()) } returns emptyList()
            every { hashtagRepository.saveAll(any<List<Hashtag>>()) } returns updateDto.hashTagList!!.map { Hashtag(tagName = it) }
            every { postingHashtagRepository.saveAll(any<List<PostingHashtag>>()) } returns emptyList()

            // when
            val result = postingService.updatePosting(updateDto)

            // then
            assertEquals(testPostingId, result.postingId)
            assertEquals(testMemberId, result.writerId)
            assertEquals(testMemberName, result.writerName)
            assertEquals(updatedContent, result.content)
            assertEquals(testImageUrls, result.imageUrl)
            
            // 검증
            verify { postingRepository.findById(testPostingId) }
            verify { jwtUtil.checkPermission(any()) }
            verify { imageService.deleteAllByPostingId(testPostingId) }
            verify { fileStorageService.deleteImagesByPostingId(testPostingId) }
            verify { fileStorageService.uploadPostingImageList(any()) }
            verify { postingHashtagRepository.deleteByPostingId(testPostingId) }
            verify { hashtagRepository.findByTagNameForExist(any()) }
            verify { hashtagRepository.saveAll(any<List<Hashtag>>()) }
            verify { postingHashtagRepository.saveAll(any<List<PostingHashtag>>()) }
        }

        @Test
        @DisplayName("존재하지 않는 게시글 ID로 수정 시 예외가 발생한다")
        fun updatePostingNotFoundFails() {
            // given
            val updateDto = RequestUpdatePostingDto(
                postingId = testPostingId,
                content = "수정된 게시글 내용"
            )
            
            every { postingRepository.findById(testPostingId) } returns Optional.empty()

            // when & then
            val exception = assertThrows(CustomException::class.java) {
                postingService.updatePosting(updateDto)
            }
            
            assertEquals(ErrorCode.POSTING, exception.errorCode)
            verify { postingRepository.findById(testPostingId) }
            verify(exactly = 0) { jwtUtil.checkPermission(any()) }
        }

        @Test
        @DisplayName("권한 없는 사용자가 게시글 수정 시 예외가 발생한다")
        fun updatePostingWithoutPermissionFails() {
            // given
            val testMember = Member(id = testMemberId, name = testMemberName, email = "test@example.com")
            val testPosting = Posting(
                id = testPostingId,
                content = testContent,
                member = testMember
            )
            
            val updateDto = RequestUpdatePostingDto(
                postingId = testPostingId,
                content = "수정된 게시글 내용"
            )
            
            every { postingRepository.findById(testPostingId) } returns Optional.of(testPosting)
            every { jwtUtil.checkPermission(any()) } throws CustomException(ErrorCode.ACCESS_DENIED)

            // when & then
            val exception = assertThrows(CustomException::class.java) {
                postingService.updatePosting(updateDto)
            }
            
            assertEquals(ErrorCode.ACCESS_DENIED, exception.errorCode)
            verify { postingRepository.findById(testPostingId) }
            verify { jwtUtil.checkPermission(any()) }
        }
    }
    
    @Nested
    @DisplayName("게시글 삭제 테스트")
    inner class DeletePostingTests {

        @Test
        @DisplayName("게시글을 성공적으로 삭제한다")
        fun deletePostingSuccess() {
            // given
            val testMember = Member(id = testMemberId, name = testMemberName, email = "test@example.com")
            val testPosting = Posting(
                id = testPostingId,
                content = testContent,
                member = testMember
            )
            
            // 이미지 정보 추가
            val image = Image(imageUrl = testImageUrls[0], imageType = ImageType.IN_POSTING, posting = testPosting)
            testPosting.imageInPosting.add(image)
            
            every { postingRepository.findById(testPostingId) } returns Optional.of(testPosting)
            every { jwtUtil.checkPermission(testMemberId) } just runs
            every { fileStorageService.deleteImagesByPostingId(testPostingId) } just runs
            every { postingRepository.deleteById(testPostingId) } just runs

            // when
            postingService.deletePosting(testPostingId)

            // then
            verify { postingRepository.findById(testPostingId) }
            verify { jwtUtil.checkPermission(testMemberId) }
            verify { fileStorageService.deleteImagesByPostingId(testPostingId) }
            verify { postingRepository.deleteById(testPostingId) }
        }

        @Test
        @DisplayName("존재하지 않는 게시글 ID로 삭제 시 예외가 발생한다")
        fun deletePostingNotFoundFails() {
            // given
            every { postingRepository.findById(testPostingId) } returns Optional.empty()

            // when & then
            val exception = assertThrows(CustomException::class.java) {
                postingService.deletePosting(testPostingId)
            }
            
            assertEquals(ErrorCode.POSTING, exception.errorCode)
            verify { postingRepository.findById(testPostingId) }
            verify(exactly = 0) { jwtUtil.checkPermission(any()) }
            verify(exactly = 0) { postingRepository.deleteById(any()) }
        }

        @Test
        @DisplayName("권한 없는 사용자가 게시글 삭제 시 예외가 발생한다")
        fun deletePostingWithoutPermissionFails() {
            // given
            val testMember = Member(id = testMemberId, name = testMemberName, email = "test@example.com")
            val testPosting = Posting(
                id = testPostingId,
                content = testContent,
                member = testMember
            )
            
            every { postingRepository.findById(testPostingId) } returns Optional.of(testPosting)
            every { jwtUtil.checkPermission(testMemberId) } throws CustomException(ErrorCode.ACCESS_DENIED)

            // when & then
            val exception = assertThrows(CustomException::class.java) {
                postingService.deletePosting(testPostingId)
            }
            
            assertEquals(ErrorCode.ACCESS_DENIED, exception.errorCode)
            verify { postingRepository.findById(testPostingId) }
            verify { jwtUtil.checkPermission(testMemberId) }
            verify(exactly = 0) { postingRepository.deleteById(any()) }
        }
    }
    
    @Nested
    @DisplayName("해시태그 관련 테스트")
    inner class HashtagTests {

        @Test
        @DisplayName("중복된 해시태그를 제거하고 저장한다")
        fun saveHashTagWithDuplicatesTest() {
            // given
            val testMember = Member(id = testMemberId, name = testMemberName, email = "test@example.com")
            val testPosting = Posting(
                id = testPostingId,
                content = testContent,
                member = testMember
            )
            
            // 중복된 해시태그 리스트
            val duplicatedHashtags = listOf("테스트", "테스트", "해시태그")
            val distinctHashtags = listOf("테스트", "해시태그")
            
            val createDto = RequestCreatePostingDto(
                writerId = testMemberId,
                content = testContent,
                hashTagList = duplicatedHashtags
            )
            
            // 기존에 존재하는 해시태그
            val existingHashtag = Hashtag(tagName = "테스트")
            val existingHashtags = listOf(existingHashtag)
            
            // Mock 설정
            every { memberRepository.findById(testMemberId) } returns Optional.of(testMember)
            every { postingRepository.save(any()) } returns testPosting
            every { hashtagRepository.findByTagNameForExist(distinctHashtags) } returns existingHashtags
            every { hashtagRepository.saveAll(any<List<Hashtag>>()) } returns listOf(Hashtag(tagName = "해시태그"))
            every { postingHashtagRepository.saveAll(any<List<PostingHashtag>>()) } returns emptyList()
            every { memberRepository.findFriendsId(any(), any()) } returns emptyList()
            every { notificationService.createNotification(any()) } just runs

            // when
            val result = postingService.createPosting(createDto)

            // then
            verify { hashtagRepository.findByTagNameForExist(distinctHashtags) }
            verify { hashtagRepository.saveAll(match { tags -> tags.size == 1 && tags[0].tagName == "해시태그" }) }
        }
    }
    
    @Nested
    @DisplayName("동시성 테스트")
    inner class ConcurrencyTests {

        @Test
        @DisplayName("동시에 여러 게시글을 생성할 수 있다")
        fun concurrentPostingCreation() {
            // given
            val threadCount = 10
            val latch = CountDownLatch(threadCount)
            val executor = Executors.newFixedThreadPool(threadCount)
            val successCount = AtomicInteger(0)
            
            val testMember = Member(id = testMemberId, name = testMemberName, email = "test@example.com")
            
            // Mock 설정
            every { memberRepository.findById(any()) } returns Optional.of(testMember)
            every { postingRepository.save(any()) } answers { 
                val posting = firstArg<Posting>()
                posting.id = successCount.incrementAndGet().toLong()
                posting
            }
            every { fileStorageService.uploadPostingImageList(any()) } returns emptyList()
            every { hashtagRepository.findByTagNameForExist(any()) } returns emptyList()
            every { hashtagRepository.saveAll(any<List<Hashtag>>()) } returns emptyList()
            every { postingHashtagRepository.saveAll(any<List<PostingHashtag>>()) } returns emptyList()
            every { memberRepository.findFriendsId(any(), any()) } returns emptyList()
            every { notificationService.createNotification(any()) } just runs

            // when
            for (i in 1..threadCount) {
                executor.execute {
                    try {
                        val createDto = RequestCreatePostingDto(
                            writerId = testMemberId,
                            content = "동시성 테스트 게시글 $i"
                        )
                        postingService.createPosting(createDto)
                    } finally {
                        latch.countDown()
                    }
                }
            }
            
            // 모든 스레드가 완료될 때까지 기다림 (30초 제한)
            latch.await(30, TimeUnit.SECONDS)
            executor.shutdown()

            // then
            assertEquals(threadCount, successCount.get())
            verify(exactly = threadCount) { memberRepository.findById(any()) }
            verify(exactly = threadCount) { postingRepository.save(any()) }
        }
        
        @Test
        @DisplayName("동시에 동일한 게시글을 여러 번 삭제하려고 할 때 예외가 발생한다")
        fun concurrentPostingDeletion() {
            // given
            val threadCount = 5
            val latch = CountDownLatch(threadCount)
            val executor = Executors.newFixedThreadPool(threadCount)
            val exceptionCount = AtomicInteger(0)
            
            val testMember = Member(id = testMemberId, name = testMemberName, email = "test@example.com")
            val testPosting = Posting(
                id = testPostingId,
                content = testContent,
                member = testMember
            )
            
            // 첫 번째 호출에만 게시글을 찾고, 나머지는 예외 발생
            val postingRef = AtomicReference(Optional.of(testPosting))
            
            every { postingRepository.findById(testPostingId) } answers { 
                val result = postingRef.getAndSet(Optional.empty())
                result
            }
            every { jwtUtil.checkPermission(any()) } just runs
            every { fileStorageService.deleteImagesByPostingId(any()) } just runs
            every { postingRepository.deleteById(any()) } just runs

            // when
            for (i in 1..threadCount) {
                executor.execute {
                    try {
                        postingService.deletePosting(testPostingId)
                    } catch (e: CustomException) {
                        if (e.errorCode == ErrorCode.POSTING) {
                            exceptionCount.incrementAndGet()
                        }
                    } finally {
                        latch.countDown()
                    }
                }
            }
            
            latch.await(30, TimeUnit.SECONDS)
            executor.shutdown()

            // then
            assertEquals(threadCount - 1, exceptionCount.get())
            verify(exactly = threadCount) { postingRepository.findById(testPostingId) }
            verify(exactly = 1) { postingRepository.deleteById(any()) }
        }
    }
}