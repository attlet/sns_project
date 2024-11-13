package com.kotlin.sns.domain.Comment.controller

import com.kotlin.sns.domain.Comment.entity.Comment
import com.kotlin.sns.domain.Comment.service.CommentService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/comment")
class CommentController (
    private val commentService: CommentService
){

}