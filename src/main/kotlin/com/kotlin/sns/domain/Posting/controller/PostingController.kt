package com.kotlin.sns.domain.Posting.controller


import com.kotlin.sns.domain.Posting.dto.request.RequestCreatePostingDto
import com.kotlin.sns.domain.Posting.dto.request.RequestUpdatePostingDto
import com.kotlin.sns.domain.Posting.dto.response.ResponsePostingDto
import com.kotlin.sns.domain.Posting.service.PostingService
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

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
                       @RequestParam("size") size : Int = 10) : List<ResponsePostingDto> {
        return postingService.findPostingList(PageRequest.of(page, size))
    }
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Parameter(name = "auth_token", description = "토큰", `in` = ParameterIn.HEADER)
    fun createPosting(@ModelAttribute requestCreatePostingDto: RequestCreatePostingDto) : ResponsePostingDto{
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