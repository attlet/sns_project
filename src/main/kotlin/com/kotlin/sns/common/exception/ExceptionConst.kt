package com.kotlin.sns.common.exception

enum class ExceptionConst(val exceptionDesc : String) {
    MEMBER("member Exception"),
    POSTING("posting Exception"),
    COMMENT("comment Exception"),
    AUTH("auth Exception"),;

    override fun toString(): String {
        return exceptionDesc
    }}