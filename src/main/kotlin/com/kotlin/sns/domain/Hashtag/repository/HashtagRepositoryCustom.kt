package com.kotlin.sns.domain.Hashtag.repository

import com.kotlin.sns.domain.Hashtag.entity.Hashtag

interface HashtagRepositoryCustom {

    fun findByTagNameForExist(tagNames : List<String>) : List<Hashtag>
}