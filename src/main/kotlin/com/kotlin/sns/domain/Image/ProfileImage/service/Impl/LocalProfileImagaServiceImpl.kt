package com.kotlin.sns.domain.Image.ProfileImage.service.Impl

import com.kotlin.sns.domain.Image.ProfileImage.service.ProfileImageService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

/**
 * localhost 환경 테스트 시 사용되는
 * 이미지 저장 로직
 *
 * @property uploadDir
 */
@Profile("local")
@Service
class LocalProfileImagaServiceImpl(
    @Value("\${file.upload-dir}") private val uploadDir : String
) : ProfileImageService{

    override fun uploadImage(file: MultipartFile) : String{
        val fileName = "profile_${System.currentTimeMillis()}"
        val path = Paths.get(fileName)
        Files.createDirectories(path.parent)
        Files.copy(file.inputStream, path, StandardCopyOption.REPLACE_EXISTING)
        return path.toUri().toString()
    }
}