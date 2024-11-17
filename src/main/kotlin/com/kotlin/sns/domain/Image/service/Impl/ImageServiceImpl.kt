package com.kotlin.sns.domain.Image.service.Impl

import com.kotlin.sns.domain.Image.entity.Image
import com.kotlin.sns.domain.Image.repository.ImageRepository
import com.kotlin.sns.domain.Image.repository.ImageRepositoryCustom
import com.kotlin.sns.domain.Image.service.ImageService
import org.springframework.web.multipart.MultipartFile

class ImageServiceImpl(
    private val imageRepository : ImageRepository
) : ImageService{
    override fun createImage(imageUrls: List<Image>) {
        imageRepository.saveAll(imageUrls)
    }

    override fun deleteAllByPostingId(postingId: Long) {
        imageRepository.deleteAllByPostingId(postingId)
    }
}