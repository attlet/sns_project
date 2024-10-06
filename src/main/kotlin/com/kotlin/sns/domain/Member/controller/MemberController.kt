package com.kotlin.sns.domain.Member.controller

import com.kotlin.sns.domain.Member.dto.request.RequestCreateMemberDto
import com.kotlin.sns.domain.Member.dto.request.RequestUpdateMemberDto
import com.kotlin.sns.domain.Member.dto.response.ResponseMemberDto
import com.kotlin.sns.domain.Member.service.MemberService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


/**
 * member controller
 *
 * @property memberService
 */
@RestController
@RequestMapping("/members")
class MemberController(
    private val memberService: MemberService
) {

    @GetMapping()
    fun getMemberById(@RequestParam("memberId") memberId : Long) : ResponseMemberDto {
        return memberService.findMemberById(memberId)
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
}