package com.kotlin.sns.domain.Posting.service.Impl

import com.kotlin.sns.common.entity.BaseEntity
import com.kotlin.sns.common.exception.CustomException
import com.kotlin.sns.common.exception.ErrorCode
import com.kotlin.sns.common.security.JwtUtil
import com.kotlin.sns.domain.Member.entity.Member
import com.kotlin.sns.domain.Member.repository.MemberRepository
import com.kotlin.sns.domain.Notification.service.NotificationService
import com.kotlin.sns.domain.Posting.dto.request.RequestCreatePostingDto
import com.kotlin.sns.domain.Posting.dto.request.RequestUpdatePostingDto
import com.kotlin.sns.domain.Posting.entity.Posting
import com.kotlin.sns.domain.Posting.repository.PostingRepository
import com.kotlin.sns.domain.Hashtag.repository.HashtagRepository
import com.kotlin.sns.domain.Image.service.FileStorageService
import com.kotlin.sns.domain.Image.service.ImageService
import com.kotlin.sns.domain.PostingHashtag.repository.PostingHashtagRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import java.util.*

/**
 * PostingServiceImpl 테스트 클래스
 *
 */
@SpringBootTest
class PostingServiceImplTest {

    @Autowired
    private lateinit var postingService: PostingServiceImpl

    @MockBean
    private lateinit var postingRepository: PostingRepository
    @MockBean
    private lateinit var memberRepository: MemberRepository
    @MockBean
    private lateinit var hashtagRepository: HashtagRepository
    @MockBean
    private lateinit var postingHashtagRepository: PostingHashtagRepository
    @MockBean
    private lateinit var notificationService: NotificationService
    @MockBean
    private lateinit var fileStorageService: FileStorageService
    @MockBean
    private lateinit var imageService: ImageService
    @MockBean
    private lateinit var jwtUtil: JwtUtil

    // BaseEntity의 protected ID를 설정하기 위한 리플렉션 기반 도우미 함수
    private fun <T : BaseEntity> T.setId(id: Long): T {
        val idField = BaseEntity::class.java.getDeclaredField("id")
        idField.isAccessible = true
        idField.set(this, id)
        return this
    }

    /**
     * 포스팅 ID로 포스팅을 조회합니다.
     * - given: 포스팅 ID와 해당 ID로 조회될 포스팅 객체를 설정합니다.
     * - when: 설정된 포스팅 ID로 `findPostingById` 메소드를 호출합니다.
     * - then: 반환된 포스팅의 내용과 작성자 이름이 예상과 일치하는지 확인합니다.
     */
    @Test
    fun `findPostingById should return posting when exists`() {
        // given
        val postingId = 1L
        val writerId = 1L
        val member = Member(userId = "user123", name = "User One", email = "user1@example.com", pw = "password", roles = listOf("USER")).setId(writerId)
        val posting = Posting(content = "Test Content", member = member).setId(postingId)
        whenever(postingRepository.findByIdForDetail(postingId)).thenReturn(Optional.of(posting))

        // when
        val result = postingService.findPostingById(postingId)

        // then
        assertThat(result.content).isEqualTo(posting.content)
        assertThat(result.writerName).isEqualTo(member.name)
    }

    /**
     * 존재하지 않는 포스팅 ID로 조회 시 예외가 발생하는지 테스트합니다.
     * - given: 존재하지 않는 포스팅 ID를 설정하고, 해당 ID로 조회 시 빈 Optional 객체를 반환하도록 설정합니다.
     * - when: 설정된 포스팅 ID로 `findPostingById` 메소드를 호출합니다.
     * - then: `CustomException`이 발생하고, 예외 메시지가 예상과 일치하는지 확인합니다.
     */
    @Test
    fun `findPostingById should throw exception when posting not found`() {
        // given
        val postingId = 1L
        whenever(postingRepository.findByIdForDetail(postingId)).thenReturn(Optional.empty())

        // when & then
        val exception = assertThrows(CustomException::class.java) {
            postingService.findPostingById(postingId)
        }
        assertThat(exception.message).contains(ErrorCode.POST_NOT_FOUND.message)
    }

    /**
     * 새로운 포스팅을 생성합니다.
     * - given: 포스팅 생성을 위한 요청 DTO와 작성자 객체를 설정합니다.
     * - when: `createPosting` 메소드를 호출하여 포스팅을 생성합니다.
     * - then: 반환된 포스팅의 내용과 작성자 ID가 요청 DTO의 값과 일치하는지 확인합니다.
     */
    @Test
    fun `createPosting should save and return posting`() {
        // given
        val writerId = 1L
        val postingId = 10L
        val request = RequestCreatePostingDto(
            writerId = writerId,
            content = "New Posting",
            hashTagList = listOf("test", "spring"),
            imageUrl = emptyList()
        )
        val writer = Member(userId = "user123", name = "User One", email = "user1@example.com", pw = "password", roles = listOf("USER")).setId(writerId)
        val savedPosting = Posting(content = request.content, member = writer).setId(postingId)

        whenever(memberRepository.findById(writerId)).thenReturn(Optional.of(writer))
        whenever(postingRepository.save(any())).thenReturn(savedPosting)
        whenever(hashtagRepository.findByTagNameForExist(any())).thenReturn(emptyList())


        // when
        val result = postingService.createPosting(request)

        // then
        assertThat(result.postingId).isEqualTo(postingId)
        assertThat(result.content).isEqualTo(request.content)
        assertThat(result.writerId).isEqualTo(writerId)
    }
    
    /**
     * 포스팅 생성 시 작성자를 찾을 수 없을 때 예외가 발생하는지 테스트합니다.
     * - given: 존재하지 않는 작성자 ID로 포스팅 생성을 요청합니다.
     * - when: `createPosting` 메소드를 호출합니다.
     * - then: `CustomException`이 발생하고, 예외 메시지가 예상과 일치하는지 확인합니다.
     */
    @Test
    fun `createPosting should throw exception when writer not found`() {
        // given
        val writerId = 999L // 존재하지 않는 ID
        val request = RequestCreatePostingDto(
            writerId = writerId,
            content = "New Posting",
            hashTagList = emptyList(),
            imageUrl = emptyList()
        )
        whenever(memberRepository.findById(writerId)).thenReturn(Optional.empty())

        // when & then
        val exception = assertThrows(CustomException::class.java) {
            postingService.createPosting(request)
        }
        assertThat(exception.message).contains(ErrorCode.MEMBER_NOT_FOUND.message)
    }

    /**
     * 포스팅 정보를 수정합니다.
     * - given: 수정할 포스팅 ID, 수정할 정보 DTO, 기존 포스팅 객체를 설정합니다.
     * - when: `updatePosting` 메소드를 호출하여 포스팅 정보를 수정합니다.
     * - then: 반환된 포스팅의 내용이 수정한 값과 일치하는지 확인합니다.
     */
    @Test
    fun `updatePosting should update and return posting`() {
        // given
        val postingId = 1L
        val writerId = 1L
        val request = RequestUpdatePostingDto(
            postingId = postingId,
            content = "Updated Content",
            hashTagList = null,
            imageUrl = null
        )
        val member = Member(userId = "user123", name = "User One", email = "user1@example.com", pw = "password", roles = listOf("USER")).setId(writerId)
        val posting = Posting(content = "Original Content", member = member).setId(postingId)
        
        whenever(postingRepository.findById(postingId)).thenReturn(Optional.of(posting))

        // when
        val result = postingService.updatePosting(request)

        // then
        assertThat(result.content).isEqualTo(request.content)
    }
    
    /**
     * 포스팅 수정 시 포스팅을 찾을 수 없을 때 예외가 발생하는지 테스트합니다.
     * - given: 존재하지 않는 포스팅 ID로 수정을 요청합니다.
     * - when: `updatePosting` 메소드를 호출합니다.
     * - then: `CustomException`이 발생하고, 예외 메시지가 예상과 일치하는지 확인합니다.
     */
    @Test
    fun `updatePosting should throw exception when posting not found`() {
        // given
        val postingId = 999L // 존재하지 않는 ID
        val request = RequestUpdatePostingDto(
            postingId = postingId,
            content = "Updated Content"
        )
        whenever(postingRepository.findById(postingId)).thenReturn(Optional.empty())

        // when & then
        val exception = assertThrows(CustomException::class.java) {
            postingService.updatePosting(request)
        }
        assertThat(exception.message).contains(ErrorCode.POST_NOT_FOUND.message)
    }

    /**
     * 존재하는 포스팅을 삭제합니다.
     * - given: 존재하는 포스팅 ID와 해당 포스팅 객체를 설정합니다.
     * - when: `deletePosting` 메소드를 호출하여 포스팅을 삭제합니다.
     * - then: `postingRepository.deleteById`가 호출되었는지 확인합니다.
     */
    @Test
    fun `deletePosting should remove posting when exists`() {
        // given
        val postingId = 1L
        val writerId = 1L
        val member = Member(userId = "user123", name = "User One", email = "user1@example.com", pw = "password", roles = listOf("USER")).setId(writerId)
        val posting = Posting(content = "Test Content", member = member).setId(postingId)
        whenever(postingRepository.findById(postingId)).thenReturn(Optional.of(posting))

        // when
        postingService.deletePosting(postingId)

        // then
        Mockito.verify(postingRepository).deleteById(postingId)
    }
    
    /**
     * 포스팅 삭제 시 포스팅을 찾을 수 없을 때 예외가 발생하는지 테스트합니다.
     * - given: 존재하지 않는 포스팅 ID로 삭제를 요청합니다.
     * - when: `deletePosting` 메소드를 호출합니다.
     * - then: `CustomException`이 발생하고, 예외 메시지가 예상과 일치하는지 확인합니다.
     */
    @Test
    fun `deletePosting should throw exception when posting not found`() {
        // given
        val postingId = 999L // 존재하지 않는 ID
        whenever(postingRepository.findById(postingId)).thenReturn(Optional.empty())

        // when & then
        val exception = assertThrows(CustomException::class.java) {
            postingService.deletePosting(postingId)
        }
        assertThat(exception.message).contains(ErrorCode.POST_NOT_FOUND.message)
    }
}