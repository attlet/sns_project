package com.kotlin.sns.domain.Review.mapper

import com.kotlin.sns.domain.Content.entity.Content
import com.kotlin.sns.domain.Member.entity.Member
import com.kotlin.sns.domain.Review.dto.request.RequestCreateReviewDto
import com.kotlin.sns.domain.Review.dto.response.ResponseReviewDto
import com.kotlin.sns.domain.Review.entity.Review

/**
 * Review 매퍼
 *
 * Review 엔티티와 DTO 간의 변환을 담당한다.
 */
object ReviewMapper {

    /**
     * Review 엔티티를 응답 DTO로 변환
     *
     * @param review Review 엔티티
     * @return 리뷰 응답 DTO
     */
    fun toDto(review: Review): ResponseReviewDto {
        return ResponseReviewDto(
            id = review.id,
            memberId = review.member.id,
            contentId = review.content.id,
            rating = review.rating,
            status = review.status,
            comment = review.comment,
            source = review.source,
            createdDt = review.createdDt
        )
    }

    /**
     * 생성 요청 DTO를 Review 엔티티로 변환
     *
     * @param request 생성 요청 DTO
     * @param member 평가자 엔티티
     * @param content 작품 엔티티
     * @return Review 엔티티
     */
    fun toEntity(request: RequestCreateReviewDto, member: Member, content: Content): Review {
        return Review(
            member = member,
            content = content,
            rating = request.rating,
            status = request.status,
            comment = request.comment
        )
    }
}
