package com.kotlin.sns.domain.Image.ProfileImage.service

import org.springframework.web.multipart.MultipartFile

interface ProfileImageService {
    fun uploadImage(file : MultipartFile) : String
}