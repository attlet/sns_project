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
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
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

    @Autowired
    lateinit var entityManager: EntityManager

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
            // given — externalRating/playtime/syncedAt은 리팩토링 후 PlatformActivityRecord로 분리됨
            val review = Review(
                member = savedMember,
                content = savedContent,
                rating = 5,
                status = ReviewStatus.FAVORITE,
                comment = "역대급 명작",
                source = ReviewSource.STEAM
            )

            // when
            val saved = reviewRepository.save(review)

            // then
            assertThat(saved.id).isNotEqualTo(0L)
            assertThat(saved.comment).isEqualTo("역대급 명작")
            assertThat(saved.source).isEqualTo(ReviewSource.STEAM)
            assertThat(saved.rating).isEqualTo(5)
            assertThat(saved.status).isEqualTo(ReviewStatus.FAVORITE)
        }
    }

    @Nested
    @DisplayName("findActiveById 테스트")
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
            val found = reviewRepository.findById(saved.id).orElse(null)

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
                    status = ReviewStatus.DROPPED
                ).apply { isDeleted = true }
            )
            entityManager.flush()
            entityManager.clear()

            // when
            val found = reviewRepository.findById(saved.id).orElse(null)

            // then
            assertThat(found).isNull()
        }

        @Test
        @DisplayName("존재하지 않는 ID 조회 시 null 반환")
        fun findNonExistentReviewReturnsNull() {
            // when
            val found = reviewRepository.findById(999L).orElse(null)

            // then
            assertThat(found).isNull()
        }
    }

    @Nested
    @DisplayName("findActiveByMemberAndContent 테스트")
    inner class FindByMemberIdAndContentIdAndIsDeletedFalseTest {

        @Test
        @DisplayName("Member + Content 조합으로 삭제되지 않은 Review 조회 성공")
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
            val found = reviewRepository.findActiveByMemberAndContent(
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
            val found = reviewRepository.findActiveByMemberAndContent(999L, 999L)

            // then
            assertThat(found).isNull()
        }
    }

    @Nested
    @DisplayName("findReviewsByMember 테스트")
    inner class FindReviewsByMemberTest {

        @Test
        @DisplayName("특정 Member의 Review 목록 페이징 조회 성공")
        fun findReviewsByMember_WithPaging() {
            // given
            val content2 = contentRepository.save(
                Content(
                    type = ContentType.GAME,
                    title = "Dark Souls",
                    description = "어두운 RPG",
                    releaseYear = 2011
                )
            )
            reviewRepository.save(Review(member = savedMember, content = savedContent, rating = 4, status = ReviewStatus.PLAYED))
            reviewRepository.save(Review(member = savedMember, content = content2, rating = 3, status = ReviewStatus.DROPPED))
            val pageable = PageRequest.of(0, 10)

            // when
            val result = reviewRepository.findReviewsByMember(savedMember.id, null, pageable)

            // then
            assertThat(result.content).hasSize(2)
            assertThat(result.totalElements).isEqualTo(2)
            assertThat(result.content).allMatch { it.member.id == savedMember.id }
        }

        @Test
        @DisplayName("삭제된 Review는 목록에서 제외됨")
        fun findReviewsByMember_ExcludesDeleted() {
            // given
            val content2 = contentRepository.save(
                Content(
                    type = ContentType.GAME,
                    title = "Dark Souls",
                    description = "어두운 RPG",
                    releaseYear = 2011
                )
            )
            reviewRepository.save(Review(member = savedMember, content = savedContent, rating = 4, status = ReviewStatus.PLAYED))
            reviewRepository.save(Review(member = savedMember, content = content2, rating = 3, status = ReviewStatus.DROPPED).apply { isDeleted = true })
            val pageable = PageRequest.of(0, 10)

            // when
            val result = reviewRepository.findReviewsByMember(savedMember.id, null, pageable)

            // then
            assertThat(result.content).hasSize(1)
            assertThat(result.content[0].isDeleted).isFalse()
        }

        @Test
        @DisplayName("status=FAVORITE 필터 적용 시 FAVORITE 리뷰만 반환")
        fun findReviewsByMember_WithStatusFilter() {
            // given
            val content2 = contentRepository.save(
                Content(
                    type = ContentType.GAME,
                    title = "Dark Souls",
                    description = "어두운 RPG",
                    releaseYear = 2011
                )
            )
            reviewRepository.save(Review(member = savedMember, content = savedContent, rating = 5, status = ReviewStatus.FAVORITE))
            reviewRepository.save(Review(member = savedMember, content = content2, rating = 3, status = ReviewStatus.DROPPED))
            val pageable = PageRequest.of(0, 10)

            // when
            val result = reviewRepository.findReviewsByMember(savedMember.id, ReviewStatus.FAVORITE, pageable)

            // then
            assertThat(result.content).hasSize(1)
            assertThat(result.content[0].status).isEqualTo(ReviewStatus.FAVORITE)
        }

        @Test
        @DisplayName("status=null이면 모든 상태의 리뷰 반환")
        fun findReviewsByMember_WithNullStatus_ReturnsAll() {
            // given
            val content2 = contentRepository.save(
                Content(
                    type = ContentType.GAME,
                    title = "Dark Souls",
                    description = "어두운 RPG",
                    releaseYear = 2011
                )
            )
            reviewRepository.save(Review(member = savedMember, content = savedContent, rating = 5, status = ReviewStatus.FAVORITE))
            reviewRepository.save(Review(member = savedMember, content = content2, rating = 3, status = ReviewStatus.WISHLIST))
            val pageable = PageRequest.of(0, 10)

            // when
            val result = reviewRepository.findReviewsByMember(savedMember.id, null, pageable)

            // then
            assertThat(result.content).hasSize(2)
        }
    }

    @Nested
    @DisplayName("findReviewsByContent 테스트")
    inner class FindReviewsByContentTest {

        @Test
        @DisplayName("특정 Content의 Review 목록 페이징 조회 성공")
        fun findReviewsByContent_WithPaging() {
            // given
            val member2 = memberRepository.save(
                Member(userId = "user2", name = "유저2", email = "user2@test.com", pw = "pw456")
            )
            reviewRepository.save(Review(member = savedMember, content = savedContent, rating = 4, status = ReviewStatus.PLAYED))
            reviewRepository.save(Review(member = member2, content = savedContent, rating = 5, status = ReviewStatus.FAVORITE))
            val pageable = PageRequest.of(0, 10)

            // when
            val result = reviewRepository.findReviewsByContent(savedContent.id, pageable)

            // then
            assertThat(result.content).hasSize(2)
            assertThat(result.totalElements).isEqualTo(2)
            assertThat(result.content).allMatch { it.content.id == savedContent.id }
        }

        @Test
        @DisplayName("삭제된 Review는 Content 목록에서 제외됨")
        fun findReviewsByContent_ExcludesDeleted() {
            // given
            val member2 = memberRepository.save(
                Member(userId = "user2", name = "유저2", email = "user2@test.com", pw = "pw456")
            )
            reviewRepository.save(Review(member = savedMember, content = savedContent, rating = 4, status = ReviewStatus.PLAYED))
            reviewRepository.save(Review(member = member2, content = savedContent, rating = 5, status = ReviewStatus.FAVORITE).apply { isDeleted = true })
            val pageable = PageRequest.of(0, 10)

            // when
            val result = reviewRepository.findReviewsByContent(savedContent.id, pageable)

            // then
            assertThat(result.content).hasSize(1)
            assertThat(result.content[0].isDeleted).isFalse()
        }
    }
}
