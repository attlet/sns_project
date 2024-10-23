package com.kotlin.sns.domain.Posting.mapper

import com.kotlin.sns.domain.Posting.dto.request.RequestCreatePostingDto
import com.kotlin.sns.domain.Posting.dto.response.ResponsePostingDto
import com.kotlin.sns.domain.Posting.entity.Posting
import org.mapstruct.Mapper

/**
 * posting <-> dto 변환 mapper
 *
 */
@Mapper(componentModel = "spring")
interface PostingMapper {
    fun toEntity(requestCreatePostingDto: RequestCreatePostingDto) : Posting
    fun toDto(posting: Posting) : ResponsePostingDto
}