package com.kotlin.sns.domain.Posting.controller


import com.kotlin.sns.domain.Posting.dto.request.RequestCreatePostingDto
import com.kotlin.sns.domain.Posting.dto.request.RequestUpdatePostingDto
import com.kotlin.sns.domain.Posting.dto.response.ResponsePostingDto
import com.kotlin.sns.domain.Posting.service.PostingService
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.PageRequest
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

    @GetMapping("/postingList")
    fun getPostingList(@RequestParam("page") page : Int = 1,
                       @RequestParam("size") size : Int = 15) : List<ResponsePostingDto> {
        return postingService.findPostingList(PageRequest.of(page, size))
    }
    @PostMapping
    @Parameter(name = "auth_token", description = "토큰", `in` = ParameterIn.HEADER)
    fun createPosting(@RequestBody requestCreatePostingDto: RequestCreatePostingDto) : ResponsePostingDto{
        return postingService.createPosting(requestCreatePostingDto)
    }
    @PutMapping
    @Parameter(name = "auth_token", description = "토큰", `in` = ParameterIn.HEADER)
    fun updatePosting(@RequestBody requestUpdatePostingDto: RequestUpdatePostingDto) : ResponsePostingDto {
        return postingService.updatePosting(requestUpdatePostingDto)
    }
    @DeleteMapping
    @Parameter(name = "auth_token", description = "토큰", `in` = ParameterIn.HEADER)
    fun deletePosting(@RequestParam("postingId") postingId : Long) {
        return postingService.deletePosting(postingId)
    }
}