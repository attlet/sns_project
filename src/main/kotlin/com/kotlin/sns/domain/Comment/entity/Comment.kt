package com.kotlin.sns.domain.Comment.entity

import com.kotlin.sns.common.entity.BaseEntity
import com.kotlin.sns.domain.Comment.dto.response.ResponseCommentDto
import com.kotlin.sns.domain.Member.entity.Member
import com.kotlin.sns.domain.Posting.entity.Posting
import jakarta.persistence.*
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction


@Entity
@SQLDelete(sql = "UPDATE comment SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
data class Comment(
    var content : String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writerId")
    var member : Member,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "postingId")
    var posting: Posting
) : BaseEntity(){

    fun toResponse() : ResponseCommentDto {
        return ResponseCommentDto(
            writerId = this.member.id,
            writerName = this.member.name,
            content = this.content,
            createDt = super.createdDt,
            updateDt = super.updateDt
        )
    }
}