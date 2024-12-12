package com.kotlin.sns.domain.Comment.service.Impl

import com.kotlin.sns.common.exception.CustomException
import com.kotlin.sns.common.exception.ExceptionConst
import com.kotlin.sns.common.security.JwtUtil
import com.kotlin.sns.domain.Comment.dto.request.RequestUpdateCommentDto
import com.kotlin.sns.domain.Comment.dto.request.RequestCommentDto
import com.kotlin.sns.domain.Comment.dto.response.ResponseCommentDto
import com.kotlin.sns.domain.Comment.entity.Comment
import com.kotlin.sns.domain.Comment.mapper.CommentMapper
import com.kotlin.sns.domain.Comment.repository.CommentRepository
import com.kotlin.sns.domain.Comment.service.CommentService
import com.kotlin.sns.domain.Member.repository.MemberRepository
import com.kotlin.sns.domain.Posting.repository.PostingRepository
import jakarta.transaction.Transactional
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class CommentServiceImpl (
    private val commentRepository: CommentRepository,
    private val postingRepository: PostingRepository,
    private val memberRepository: MemberRepository,
    private val commentMapper: CommentMapper,
    private val jwtUtil: JwtUtil
) : CommentService{

    /**
     *  포스팅 로딩 시 댓글 정보 다 가져오지만,
     *  댓글 새로고침을 통해 그 포스팅 댓글만 새로 가져올 수 있도록
     *  로직 구현
     *
     * @param pageable
     * @param postingId
     * @return
     */
    override fun getCommentListInPosting(pageable: Pageable, postingId: Long): List<ResponseCommentDto>? {
        val responseCommentList = mutableListOf<ResponseCommentDto>()
        val comment =  commentRepository.findCommentListByPosting(pageable, postingId)

        comment.map { comment -> {
            responseCommentList.add(createResponseCommentDtoList(comment))
        } }

        return responseCommentList
    }

    @Transactional
    override fun createComment(requestCommentDto: RequestCommentDto) : ResponseCommentDto {
        val writerId = requestCommentDto.writerId
        val postingId = requestCommentDto.postingId

        val member = memberRepository.findById(writerId)
            .orElseThrow{
                CustomException(
                    ExceptionConst.MEMBER,
                    HttpStatus.NOT_FOUND,
                    "Member with id $writerId not found"
                )
            }

        val posting = postingRepository.findById(postingId)
            .orElseThrow {
                CustomException(
                    ExceptionConst.POSTING,
                    HttpStatus.NOT_FOUND,
                    "Posting with id $postingId not found"
                )
            }

        val savedComment = commentRepository.save(Comment(
            content = requestCommentDto.content,
            member = member,
            posting = posting
        ))

        return commentMapper.toDto(savedComment)
    }
    @Transactional
    override fun updateComment(requestUpdateCommentDto: RequestUpdateCommentDto) : ResponseCommentDto {
        val commentId = requestUpdateCommentDto.commentId
        val comment = commentRepository.findById(commentId)
            .orElseThrow {
                CustomException(
                    ExceptionConst.COMMENT,
                    HttpStatus.NOT_FOUND,
                    "Comment with id $commentId not found"
                )
            }

        jwtUtil.checkPermission(commentId)

        comment.content = requestUpdateCommentDto.content

        val savedComment = commentRepository.save(comment)

        return commentMapper.toDto(savedComment)
    }

    @Transactional
    override fun deleteComment(commentId: Long) {
        jwtUtil.checkPermission(commentId)
        commentRepository.deleteById(commentId)
    }

    /**
     * ResponseCommentDtoList를 생성하기 위한 메서드
     *
     * @param commentList
     * @return
     */
    private fun createResponseCommentDtoList(comment : Comment) : ResponseCommentDto{
        return ResponseCommentDto(
            writerId = comment.member.id,
            writerName = comment.member.name,
            content = comment.content,
            createDt = comment.createdDt,
            updateDt = comment.updateDt
        )
    }
}