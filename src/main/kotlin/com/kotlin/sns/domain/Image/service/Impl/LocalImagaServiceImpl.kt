package com.kotlin.sns.domain.Image.service.Impl

import com.kotlin.sns.domain.Image.entity.Image
import com.kotlin.sns.domain.Image.repository.ImageRepository
import com.kotlin.sns.domain.Image.service.FileStorageService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

/**
 * localhost 환경 테스트 시 사용되는
 * 이미지 저장 로직
 *
 * @property profileDir
 */
@Profile("local")
@Service
class LocalImagaServiceImpl(
    @Value("\${file.profile-images-dir}") private val profileDir : String,
    @Value("\${file.posting-images-dir}") private val postingDir : String,
    private val imageRepository: ImageRepository
) : FileStorageService {

    override fun uploadProfileImage(file: MultipartFile) : String{
        val fileName = "profile_${System.currentTimeMillis()}.png" //file name 생성, 겹치치 않도록 생성하는 더 좋은 방법 있을 수도.
        val filePath = Paths.get(profileDir, fileName)          //uploadDir 폴더 내부에 fileName이라는 이름으로 파일 저장할 것이라는 경로 객체를 생성
        Files.createDirectories(filePath.parent)               //만약 상위 디렉토리가 없다면, 생성. 있다면 동작 x

        /*
        inputStream을 통해 업로드 요청된 파일 읽기 작업 후,
        filePath경로에 파일을 저장
        replace_existing 옵션을 통해 같은 이름의 파일 존재 시, 덮어쓰기
         */

        Files.copy(file.inputStream, filePath, StandardCopyOption.REPLACE_EXISTING)
        return filePath.toString()
    }

    override fun uploadPostingImageList(files: List<MultipartFile>?): List<String>? {
        if(files == null) return null

        val urlList = ArrayList<String>()

        for (file in files) {
            val fileName = "posting_${System.currentTimeMillis()}.png"
            val filePath = Paths.get(postingDir, fileName)

            Files.createDirectories(filePath.parent)
            Files.copy(file.inputStream, filePath, StandardCopyOption.REPLACE_EXISTING)

            urlList.add(filePath.toString())
        }
        return urlList
    }

    /**
     * 서버에 저장된 posting 첨부 이미지 전부 삭제
     *
     * @param postingId
     */
    override fun deleteImagesByPostingId(postingId : Long) {

        // 1. posting id를 통해 posting에 첨부된 image 엔티티들 find
        val imageEntityList = imageRepository.findAllByPostingId(postingId)

        // 2. imageList를 순회하며 image 엔티티에 저장된 url 추출
        if(!imageEntityList.isNullOrEmpty()){
            for(imageEntity in imageEntityList){
                val imageUrl = imageEntity.imageUrl
                val path = Paths.get(imageUrl)

                // 3. 서버에 해당 url 경로에 실제 이미지 저장되어 있는지 체크, 삭제
                if(Files.exists(path)){
                    Files.delete(path)
                }
            }
        }
    }
}