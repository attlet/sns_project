package com.kotlin.sns.domain.Hashtag.repository

interface HashtagRepositoryCustom {

    /**
     *  기존에 존재하지 않는 hashtag인지 확인해서 존재하지 않는 태그들만 반환
     *
     * @param tagNames
     * @return
     */
    fun findByTagNameForNotExist(tagNames : List<String>) : List<String>
}