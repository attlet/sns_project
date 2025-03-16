package com.kotlin.sns.domain.Member.repository.Impl


import com.kotlin.sns.domain.Member.entity.Member
import com.kotlin.sns.domain.Member.entity.QMember
import com.kotlin.sns.domain.Member.repository.MemberRepositoryCustom
import com.kotlin.sns.domain.Posting.entity.QPosting
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class MemberRepositoryCustomImpl(
    private val jpaQueryFactory: JPAQueryFactory
) : MemberRepositoryCustom {

    private val qMember = QMember.member
    private val qPosting = QPosting.posting

    /**
     * user id를 기반으로 상세 정보 조회
     * 해당 사용자의 작성 게시글도 조회
     *
     * @param userId
     * @return
     */
    override fun findByUserId(userId: String): Optional<Member> {
        val member = jpaQueryFactory.selectFrom(qMember)
            .leftJoin(qMember.postings, qPosting).fetchJoin()
            .where(qMember.userId.eq(userId))
            .fetchOne()

        return Optional.ofNullable(member)
    }


}