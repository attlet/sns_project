package com.kotlin.sns.posting

import com.kotlin.sns.domain.Posting.repository.PostingRepository
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest

@DataJpaTest
class postingRepositoryTest(
    private val postingRepository : PostingRepository
) {

}