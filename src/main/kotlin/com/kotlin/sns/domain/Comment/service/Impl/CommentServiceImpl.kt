package com.kotlin.sns.domain.Comment.service.Impl

import com.kotlin.sns.common.exception.CustomException
import com.kotlin.sns.common.exception.ExceptionConst
import com.kotlin.sns.domain.Comment.dto.request.ReqeustUpdateCommentDto
import com.kotlin.sns.domain.Comment.dto.request.RequestCommentDto
import com.kotlin.sns.domain.Comment.dto.response.ResponseCommentDto
import com.kotlin.sns.domain.Comment.entity.Comment
import com.kotlin.sns.domain.Comment.repository.CommentRepository
import com.kotlin.sns.domain.Comment.service.CommentService
import com.kotlin.sns.domain.Member.repository.MemberRepository
import com.kotlin.sns.domain.Posting.repository.PostingRepository
import jakarta.transaction.Transactional
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class CommentServiceImpl (
    private val commentRepository: CommentRepository,
    private val postingRepository: PostingRepository,
    private val memberRepository: MemberRepository
) : CommentService{

    /**
     * 포스팅 로딩 시 댓글 정보 다 가져오지만,
     * 댓글 새로고침을 통해 그 포스팅 댓글만 새로 가져올 수 있도록
     * 로직 구현
     *
     * @param postingId
     * @return
     */
    override fun findCommentInPosting(postingId: Long): List<ResponseCommentDto> {
        val posting = postingRepository.findById(postingId)
            .orElseThrow {
                CustomException(
                    ExceptionConst.POSTING,
                    HttpStatus.NOT_FOUND,
                    "Posting with id $postingId not found"
                )
            }

        val commentList = posting.comment
        val responseCommentDtoList = commentList.stream()
            .map { comment -> ResponseCommentDto(
            content = comment.content,
            createAt = comment.createdDt,
            updateAt = comment.updateDt
        ) }
            .toList()

        return responseCommentDtoList
    }
    @Transactional
    override fun createComment(requestCommentDto: RequestCommentDto) {
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

        commentRepository.save(Comment(
            content = requestCommentDto.content,
            member = member,
            posting = posting
        ))
    }

    @Transactional
    override fun updateComment(requestUpdateCommentDto: ReqeustUpdateCommentDto) {
        val commentId = requestUpdateCommentDto.commentId
        val comment = commentRepository.findById(commentId)
            .orElseThrow {
                CustomException(
                    ExceptionConst.COMMENT,
                    HttpStatus.NOT_FOUND,
                    "Comment with id $commentId not found"
                )
            }

        comment.content = requestUpdateCommentDto.content

        commentRepository.save(comment)
    }

    @Transactional
    override fun deleteComment(commentId: Long) {
        commentRepository.deleteById(commentId)
    }
}