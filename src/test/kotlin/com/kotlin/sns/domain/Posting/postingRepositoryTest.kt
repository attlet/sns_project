package com.kotlin.sns.domain.Posting

import com.kotlin.sns.domain.Posting.repository.PostingRepository
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
class postingRepositoryTest(
    private val postingRepository : PostingRepository
) {

}