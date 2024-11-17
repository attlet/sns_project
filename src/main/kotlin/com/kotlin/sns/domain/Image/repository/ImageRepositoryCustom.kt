package com.kotlin.sns.domain.Image.repository

import com.kotlin.sns.domain.Image.entity.Image

interface ImageRepositoryCustom {
    fun findAllByPostingId(postingId : Long) : List<Image>?
    fun deleteAllByPostingId(postingId : Long)
}