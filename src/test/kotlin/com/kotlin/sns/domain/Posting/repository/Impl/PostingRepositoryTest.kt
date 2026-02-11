package com.kotlin.sns.domain.Posting.repository.Impl

import com.kotlin.sns.domain.Posting.repository.PostingRepository
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
class PostingRepositoryTest(
    private val postingRepository : PostingRepository
) {

}