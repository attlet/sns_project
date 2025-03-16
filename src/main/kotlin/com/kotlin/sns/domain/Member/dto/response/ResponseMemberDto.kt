package com.kotlin.sns.domain.Member.dto.response

import com.kotlin.sns.domain.Comment.dto.response.ResponseCommentDto
import com.kotlin.sns.domain.Posting.dto.response.ResponsePostingDto
import org.springframework.web.multipart.MultipartFile

/**
 * 기본적인 member 처리 반환 dto
 *
 * @property name
 * @property email
 * @property profileImageUrl
 */
data class ResponseMemberDto(
    val name : String,
    val profileImage : MultipartFile? = null,
    val uploadedPostingList : List<ResponsePostingDto>? = null,   //작성한 posting 목록
    val uploadedPostingCnt : Int = 0,                             //작성한 posting 갯수
    val uploadedCommentList : List<ResponseCommentDto>? = null,   //작성한 댓글 목록
){
}