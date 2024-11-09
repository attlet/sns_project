package com.kotlin.sns.domain.Image.service

import org.springframework.web.multipart.MultipartFile

interface ImageService {
    fun uploadProfileImage(file : MultipartFile) : String
    fun updatePostingImageList(file : List<MultipartFile>?) : List<String>?
}