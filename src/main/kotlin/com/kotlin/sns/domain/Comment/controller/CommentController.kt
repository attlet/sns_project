package com.kotlin.sns.domain.Comment.controller

import com.kotlin.sns.domain.Comment.dto.request.RequestCommentDto
import com.kotlin.sns.domain.Comment.dto.request.RequestUpdateCommentDto
import com.kotlin.sns.domain.Comment.dto.response.ResponseCommentDto
import com.kotlin.sns.domain.Comment.entity.Comment
import com.kotlin.sns.domain.Comment.service.CommentService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/comment")
class CommentController (
    private val commentService: CommentService
){
    @PostMapping()
    fun createComment(@RequestBody requestCommentDto: RequestCommentDto) : ResponseCommentDto {
        return commentService.createComment(requestCommentDto)
    }

    @PutMapping()
    fun updateComment(@RequestParam("commentId") commentId : Long,
                      @RequestBody requestUpdateCommentDto : RequestUpdateCommentDto) : ResponseCommentDto {
        return commentService.updateComment(requestUpdateCommentDto)
    }

}