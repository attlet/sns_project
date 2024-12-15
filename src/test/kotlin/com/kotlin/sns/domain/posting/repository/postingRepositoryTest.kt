package com.kotlin.sns.domain.posting.repository

import com.kotlin.sns.domain.Posting.repository.PostingRepository
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest

@DataJpaTest
class postingRepositoryTest(
    private val postingRepository : PostingRepository
) {
    @Test
    fun getPostingListWithCommentTest(){
        val pageable = PageRequest.of(0, 10)
    }
}