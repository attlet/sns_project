package com.kotlin.sns.domain.Posting.repository

import com.kotlin.sns.domain.Posting.entity.Posting
import org.springframework.data.domain.Pageable
import java.util.Optional

interface PostingRepositoryCustom {

    /**
     * 게시글 하나 상세 조회
     * 첨부 이미지, 댓글, 댓글 작성자 같이 조회
     *
     * @param postingId
     * @return
     */
    fun findByIdForDetail(postingId: Long) : Optional<Posting>

    /**
     * 게시글 리스트 조회
     * 댓글 같이 조회
     * @param pageable
     * @return
     */
    fun getPostingListWithComment(pageable: Pageable) : List<Posting>
}