package com.kotlin.sns.domain.Member.entity

import jakarta.persistence.Entity

@Entity
class Member (
    memberId : String,
    name : String,
    email : String
){

}