package com.kotlin.sns.domain.Member.controller

import com.kotlin.sns.domain.Image.service.FileStorageService
import com.kotlin.sns.domain.Member.dto.request.RequestCreateMemberDto
import com.kotlin.sns.domain.Member.dto.request.RequestUpdateMemberDto
import com.kotlin.sns.domain.Member.dto.response.ResponseMemberDto
import com.kotlin.sns.domain.Member.service.MemberService
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile


/**
 * member controller
 *
 * @property memberService
 */
@RestController
@RequestMapping("/members")
@Tag(name = "member", description = "member 관련 api")
class MemberController(
    private val memberService: MemberService,
    private val fileStorageService: FileStorageService
) {


    @GetMapping()
    fun getMemberByUserId(@RequestParam("userId") userId : String) : ResponseMemberDto{
        return memberService.findMemberByUserId(userId)
    }
    @PostMapping
    fun createMember(@RequestBody requestCreateMemberDto: RequestCreateMemberDto) : ResponseMemberDto{
        return memberService.createMember(requestCreateMemberDto)
    }

    @PutMapping
    fun updateMember(@RequestBody requestUpdateMemberDto: RequestUpdateMemberDto) : ResponseMemberDto{
        return memberService.updateMember(requestUpdateMemberDto)
    }
    @DeleteMapping
    fun deleteMember(@RequestParam("memberId") memberId : Long){
        return memberService.deleteMember(memberId)
    }

    @PostMapping("/profileImage")
    fun updateProfileImage(@RequestParam("memberId") memberId: Long,
        @RequestParam("file") file : MultipartFile) : String{

        val profileImageUrl = fileStorageService.uploadProfileImage(file)

        return profileImageUrl
    }
}