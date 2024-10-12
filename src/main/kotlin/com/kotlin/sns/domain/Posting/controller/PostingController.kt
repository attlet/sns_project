package com.kotlin.sns.domain.Posting.controller

import com.kotlin.sns.domain.Posting.dto.request.RequestCreaetePostingDto
import com.kotlin.sns.domain.Posting.dto.request.RequestUpdatePostingDto
import com.kotlin.sns.domain.Posting.dto.response.ResponsePostingDto
import com.kotlin.sns.domain.Posting.service.PostingService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * posting controller
 *
 * @property postingService
 */
@RestController
@RequestMapping("/postings")
@Tag(name = "posting", description = "posting 관련 api")
class PostingController (
    private val postingService: PostingService
){
    @GetMapping()
    fun getPostingById(@RequestParam("postingId") postingId : Long) : ResponsePostingDto{
        return postingService.findPostingById(postingId)
    }
    @PostMapping
    fun createPosting(@RequestBody requestCreaetePostingDto: RequestCreaetePostingDto) : ResponsePostingDto{
        return postingService.createPosting(requestCreaetePostingDto)
    }
    @PutMapping
    fun updatePosting(@RequestBody requestUpdatePostingDto: RequestUpdatePostingDto) : ResponsePostingDto {
        return postingService.updatePosting(requestUpdatePostingDto)
    }
    @DeleteMapping
    fun deletePosting(@RequestParam("postingId") postingId : Long) {
        return postingService.deletePosting(postingId)
    }
}