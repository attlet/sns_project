package com.kotlin.sns.domain.Image.service

import com.kotlin.sns.domain.Image.entity.Image
import org.springframework.web.multipart.MultipartFile

interface FileStorageService {
    fun uploadProfileImage(file : MultipartFile) : String
    fun uploadPostingImageList(file : List<MultipartFile>) : List<String>
    fun deleteImagesByPostingId(postingId : Long)
}