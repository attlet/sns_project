package com.kotlin.sns.domain.Likes.service.Impl

import com.kotlin.sns.common.exception.CustomException
import com.kotlin.sns.common.exception.ErrorCode
import com.kotlin.sns.domain.Likes.dto.request.RequestLikesDto
import com.kotlin.sns.domain.Likes.entity.Likes
import com.kotlin.sns.domain.Likes.repository.LikesRepository
import com.kotlin.sns.domain.Likes.service.LikesService
import com.kotlin.sns.domain.Member.repository.MemberRepository
import com.kotlin.sns.domain.Posting.repository.PostingRepository
import io.github.oshai.kotlinlogging.KotlinLogging

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LikesServiceImpl (
    private val likesRepository: LikesRepository,
    private val postingRepository: PostingRepository,
    private val memberRepository: MemberRepository
) : LikesService{

    private val logger = KotlinLogging.logger{}

    @Transactional
    override fun createLikes(requestLikesDto: RequestLikesDto): Int {
        val memberId = requestLikesDto.memberId
        val postingId = requestLikesDto.postingId

        val member = memberRepository.findById(requestLikesDto.memberId)
            .orElseThrow { CustomException(ErrorCode.MEMBER_NOT_FOUND) }

        val posting = postingRepository.findById(postingId)
            .orElseThrow { CustomException(ErrorCode.POST_NOT_FOUND) }

        // 좋아요 중복 체크
        if (likesRepository.checkLikesExist(requestLikesDto)) {
            throw CustomException(ErrorCode.ALREADY_LIKED_POST)
        }

        val likes = Likes(
            member = member,
            posting = posting
        )

        likesRepository.save(likes)
        val cnt = likesRepository.getLikesCount(postingId)

        logger.info { "User ${member.id} liked posting ${posting.id}, cnt : $cnt" }

        return cnt
    }

    @Transactional
    override fun cancleLikes(requestLikesDto: RequestLikesDto): Int {
        val memberId = requestLikesDto.memberId
        val postingId = requestLikesDto.postingId

        val existLikes = likesRepository.findLikesByMemberAndPosting(requestLikesDto)
            .orElseThrow { CustomException(ErrorCode.LIKE_NOT_FOUND) }

        logger.info { "User ${memberId} canceled like on posting ${postingId}" }

        likesRepository.delete(existLikes)
        val cnt = likesRepository.getLikesCount(postingId)

        logger.debug { "like decrease : $cnt" }
        return cnt
    }
}