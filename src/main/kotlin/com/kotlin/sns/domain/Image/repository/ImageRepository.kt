package com.kotlin.sns.domain.Image.repository

import com.kotlin.sns.domain.Image.entity.Image
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ImageRepository : JpaRepository<Image, Long>, ImageRepositoryCustom{

}