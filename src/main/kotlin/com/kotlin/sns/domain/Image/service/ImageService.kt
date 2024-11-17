package com.kotlin.sns.domain.Image.service

import com.kotlin.sns.domain.Image.entity.Image
import org.springframework.web.multipart.MultipartFile

interface ImageService {
    fun createImage(imageUrls : List<Image>)
    fun deleteAllByPostingId(postingId : Long)
}