package com.kotlin.sns

import com.kotlin.sns.domain.Hashtag.repository.HashtagRepository
import com.kotlin.sns.domain.Member.repository.MemberRepository
import com.kotlin.sns.domain.Posting.repository.PostingRepository
import com.kotlin.sns.domain.PostingHashtag.repository.PostingHashtagRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("local")
class DataInit (
    private val memberRepository: MemberRepository,
    private val postingRepository: PostingRepository,
    private val hashtagRepository: HashtagRepository,
    private val postingHashtagRepository: PostingHashtagRepository
) : CommandLineRunner{
    override fun run(vararg args: String?) {
//        val memberList = mutableListOf<Member>()
//        val hashtagList = mutableListOf<Hashtag>()
//        val postingList = mutableListOf<Posting>()
//
//        if(memberRepository.findById(1).isPresent){
//            return;
//        }
//
//        for(i in 1 .. 20){
//            val member = Member(
//                userId = "123456789$i",
//                name = "user$i",
//                email = "user$i@aaa.com",
//                pw = "1234$i",
//                roles = listOf("user")
//            )
//            memberList.add(member)
//        }
//
//        memberRepository.saveAll(memberList)
//
//        for(i in 1 .. 10){
//            val hashtag = Hashtag(
//                tagName = "test tag$i"
//            )
//            hashtagList.add(hashtag)
//        }
//
//        hashtagRepository.saveAll(hashtagList)
//
//        for(i in 1 .. 50000000){
//            val posting = Posting(
//                content = "test posting$i",
//                member = memberList[i % 20],
//            )
//
//            val postingHashtag = mutableListOf(PostingHashtag(
//                posting = posting,
//                hashtag = hashtagList[i % 10]))
//
//            postingRepository.save(posting)
//
//            posting.postingHashtag = postingHashtag
//
//            postingHashtagRepository.save(postingHashtag[0])
//        }


    }
}