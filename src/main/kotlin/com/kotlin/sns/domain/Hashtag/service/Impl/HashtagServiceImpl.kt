package com.kotlin.sns.domain.Hashtag.service.Impl

import com.kotlin.sns.domain.Hashtag.dto.request.RequestHashtagDto
import com.kotlin.sns.domain.Hashtag.dto.response.ResponseHashtagDto
import com.kotlin.sns.domain.Hashtag.entity.Hashtag
import com.kotlin.sns.domain.Hashtag.repository.HashtagRepository
import com.kotlin.sns.domain.Hashtag.service.HashtagService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class HashtagServiceImpl(
    private val hashtagRepository: HashtagRepository
) : HashtagService {

    @Transactional
    override fun createHashtag(requestHashtagDto: RequestHashtagDto): ResponseHashtagDto {
        val hashtag = Hashtag(
            tagName = requestHashtagDto.tagName
        )

        val savedTag = hashtagRepository.save(hashtag)

        return ResponseHashtagDto(
            tagName = savedTag.tagName
        )
    }

    @Transactional
    override fun deleteHashtag(hashtagId: Long) {
        hashtagRepository.deleteById(hashtagId)
    }
}