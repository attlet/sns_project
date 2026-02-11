package com.kotlin.sns.domain.Review.repository.Impl

import com.kotlin.sns.domain.Content.entity.Content
import com.kotlin.sns.domain.Content.entity.ContentType
import com.kotlin.sns.domain.Content.repository.ContentRepository
import com.kotlin.sns.domain.Member.entity.Member
import com.kotlin.sns.domain.Member.repository.MemberRepository
import com.kotlin.sns.domain.Review.entity.Review
import com.kotlin.sns.domain.Review.entity.ReviewSource
import com.kotlin.sns.domain.Review.entity.ReviewStatus
import com.kotlin.sns.domain.Review.repository.ReviewRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import com.kotlin.sns.domain.Content.repository.Impl.ContentRepositoryTestConfig

/**
 * ReviewRepository 테스트
 *
 * JPA 기본 CRUD 및 커스텀 쿼리 메서드를 테스트한다.
 */
@DataJpaTest
@Import(ContentRepositoryTestConfig::class)
class ReviewRepositoryTest {

    @Autowired
    lateinit var reviewRepository: ReviewRepository

    @Autowired
    lateinit var memberRepository: MemberRepository

    @Autowired
    lateinit var contentRepository: ContentRepository

    private lateinit var savedMember: Member
    private lateinit var savedContent: Content

    @BeforeEach
    fun setUp() {
        savedMember = memberRepository.save(
            Member(
                userId = "testuser",
                name = "테스트유저",
                email = "test@test.com",
                pw = "password123"
            )
        )
        savedContent = contentRepository.save(
            Content(
                type = ContentType.GAME,
                title = "Elden Ring",
                description = "오픈월드 액션 RPG",
                releaseYear = 2022,
                steamAppId = 1245620L
            )
        )
    }

    @Nested
    @DisplayName("save 테스트")
    inner class SaveTest {

        @Test
        @DisplayName("Review 저장 성공 - ID 생성 및 관계 매핑 확인")
        fun saveReviewSuccess() {
            // given
            val review = Review(
                member = savedMember,
                content = savedContent,
                rating = 4,
                status = ReviewStatus.PLAYED
            )

            // when
            val saved = reviewRepository.save(review)

            // then
            assertThat(saved.id).isNotEqualTo(0L)
            assertThat(saved.member.id).isEqualTo(savedMember.id)
            assertThat(saved.content.id).isEqualTo(savedContent.id)
            assertThat(saved.rating).isEqualTo(4)
            assertThat(saved.status).isEqualTo(ReviewStatus.PLAYED)
            assertThat(saved.source).isEqualTo(ReviewSource.MANUAL)
            assertThat(saved.isDeleted).isFalse()
        }

        @Test
        @DisplayName("모든 필드를 포함한 Review 저장 성공")
        fun saveReviewWithAllFields() {
            // given
            val syncTime = java.time.Instant.now()
            val review = Review(
                member = savedMember,
                content = savedContent,
                rating = 5,
                status = ReviewStatus.FAVORITE,
                comment = "역대급 명작",
                source = ReviewSource.STEAM,
                externalRating = 9.5,
                playtime = 12000,
                syncedAt = syncTime
            )

            // when
            val saved = reviewRepository.save(review)

            // then
            assertThat(saved.id).isNotEqualTo(0L)
            assertThat(saved.comment).isEqualTo("역대급 명작")
            assertThat(saved.source).isEqualTo(ReviewSource.STEAM)
            assertThat(saved.externalRating).isEqualTo(9.5)
            assertThat(saved.playtime).isEqualTo(12000)
            assertThat(saved.syncedAt).isEqualTo(syncTime)
        }
    }

    @Nested
    @DisplayName("findByIdAndIsDeletedFalse 테스트")
    inner class FindByIdAndIsDeletedFalseTest {

        @Test
        @DisplayName("삭제되지 않은 Review 조회 성공")
        fun findActiveReview() {
            // given
            val saved = reviewRepository.save(
                Review(
                    member = savedMember,
                    content = savedContent,
                    rating = 4,
                    status = ReviewStatus.PLAYED
                )
            )

            // when
            val found = reviewRepository.findByIdAndIsDeletedFalse(saved.id)

            // then
            assertThat(found).isNotNull
            assertThat(found!!.id).isEqualTo(saved.id)
        }

        @Test
        @DisplayName("Soft Delete된 Review는 조회되지 않음")
        fun findDeletedReviewReturnsNull() {
            // given
            val saved = reviewRepository.save(
                Review(
                    member = savedMember,
                    content = savedContent,
                    rating = 3,
                    status = ReviewStatus.DROPPED,
                    isDeleted = true
                )
            )

            // when
            val found = reviewRepository.findByIdAndIsDeletedFalse(saved.id)

            // then
            assertThat(found).isNull()
        }

        @Test
        @DisplayName("존재하지 않는 ID 조회 시 null 반환")
        fun findNonExistentReviewReturnsNull() {
            // when
            val found = reviewRepository.findByIdAndIsDeletedFalse(999L)

            // then
            assertThat(found).isNull()
        }
    }

    @Nested
    @DisplayName("findByMemberIdAndContentId 테스트")
    inner class FindByMemberIdAndContentIdTest {

        @Test
        @DisplayName("Member + Content 조합으로 Review 조회 성공")
        fun findByMemberAndContent() {
            // given
            reviewRepository.save(
                Review(
                    member = savedMember,
                    content = savedContent,
                    rating = 4,
                    status = ReviewStatus.PLAYED
                )
            )

            // when
            val found = reviewRepository.findByMemberIdAndContentId(
                savedMember.id, savedContent.id
            )

            // then
            assertThat(found).isNotNull
            assertThat(found!!.member.id).isEqualTo(savedMember.id)
            assertThat(found.content.id).isEqualTo(savedContent.id)
        }

        @Test
        @DisplayName("존재하지 않는 조합 조회 시 null 반환")
        fun findNonExistentCombinationReturnsNull() {
            // when
            val found = reviewRepository.findByMemberIdAndContentId(999L, 999L)

            // then
            assertThat(found).isNull()
        }
    }
}
