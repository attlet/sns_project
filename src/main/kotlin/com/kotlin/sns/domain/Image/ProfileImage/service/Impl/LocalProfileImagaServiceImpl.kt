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
        val fileName = "profile_${System.currentTimeMillis()}.png" //file name 생성, 겹치치 않도록 생성하는 더 좋은 방법 있을 수도.
        val filePath = Paths.get(uploadDir, fileName)          //uploadDir 폴더 내부에 fileName이라는 이름으로 파일 저장할 것이라는 경로 객체를 생성
        Files.createDirectories(filePath.parent)               //만약 상위 디렉토리가 없다면, 생성. 있다면 동작 x

        /*
        inputStream을 통해 업로드 요청된 파일 읽기 작업 후,
        filePath경로에 파일을 저장
        replace_existing 옵션을 통해 같은 이름의 파일 존재 시, 덮어쓰기
         */

        Files.copy(file.inputStream, filePath, StandardCopyOption.REPLACE_EXISTING)
        return filePath.toUri().toString()
    }
}