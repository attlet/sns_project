package com.kotlin.sns.common.exception

import org.springframework.http.HttpStatus

/**
 * 애플리케이션 전반의 오류 코드를 정의하는 Enum
 */
enum class ErrorCode(
    val code: String,        // 클라이언트에게 노출될 수 있는 고유 오류 코드 (문자열)
    val status: HttpStatus,  // 해당 오류에 대한 기본 HTTP 상태 코드
    val message: String      // 오류 발생 시 전달될 실제 메시지 내용
) {
    // --- 일반 오류 ---
    INVALID_INPUT_VALUE("C001", HttpStatus.BAD_REQUEST, "입력 값이 유효하지 않습니다. (필드: {0})"),
    METHOD_NOT_ALLOWED("C002", HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않는 HTTP 메소드입니다."),
    ENTITY_NOT_FOUND("C003", HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR("S001", HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요."),
    INVALID_TYPE_VALUE("C004", HttpStatus.BAD_REQUEST, "잘못된 타입의 값입니다."),
    ACCESS_DENIED("A001", HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    // --- 인증 및 권한 관련 오류 (기존 ExceptionConst.AUTH 카테고리 세분화) ---
    AUTHENTICATION_FAILED("A002", HttpStatus.UNAUTHORIZED, "인증에 실패했습니다. 아이디 또는 비밀번호를 확인해주세요."),
    INVALID_TOKEN("A003", HttpStatus.UNAUTHORIZED, "유효하지 않은 인증 토큰입니다."),
    DIFFERENT_REFRESH_TOKEN("A004", HttpStatus.UNAUTHORIZED, "리프레시 토큰이 일치하지 않습니다."),
    TOKEN_EXPIRED("A005", HttpStatus.UNAUTHORIZED, "인증 토큰이 만료되었습니다. 다시 로그인해주세요."),
    CANNOT_MODIFY_OR_DELETE_POSTING("A006", HttpStatus.FORBIDDEN, "게시글 수정 및 삭제 권한이 없습니다."),

    // --- 회원 관련 오류 (기존 ExceptionConst.MEMBER 카테고리 세분화) ---
    MEMBER_NOT_FOUND("M001", HttpStatus.NOT_FOUND, "해당 회원을 찾을 수 없습니다. (ID: {0})"),
    MEMBER_NOT_FOUND_BY_EMAIL("M002", HttpStatus.NOT_FOUND, "해당 이메일을 가진 회원을 찾을 수 없습니다. (이메일: {0})"),
    MEMBER_NOT_FOUND_BY_USERID("M003", HttpStatus.NOT_FOUND, "해당 아이디를 가진 회원을 찾을 수 없습니다. (아이디: {0})"),
    EMAIL_DUPLICATION("M004", HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다: {0}"),
    USERID_DUPLICATION("M005", HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다: {0}"),
    INVALID_PASSWORD("M006", HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),

    // --- 게시글 관련 오류 (기존 ExceptionConst.POSTING 카테고리 세분화) ---
    POST_NOT_FOUND("P001", HttpStatus.NOT_FOUND, "해당 게시글을 찾을 수 없습니다. (ID: {0})"),
    POST_UPDATE_ACCESS_DENIED("P002", HttpStatus.FORBIDDEN, "게시글 수정 권한이 없습니다."), 
    POST_DELETE_ACCESS_DENIED("P003", HttpStatus.FORBIDDEN, "게시글 삭제 권한이 없습니다."), 

    // --- 댓글 관련 오류 (기존 ExceptionConst.COMMENT 카테고리 세분화) ---
    COMMENT_NOT_FOUND("CM001", HttpStatus.NOT_FOUND, "해당 댓글을 찾을 수 없습니다. (ID: {0})"),
    COMMENT_ACCESS_DENIED("CM002", HttpStatus.FORBIDDEN, "댓글에 대한 접근 권한이 없습니다."), 

    // --- 좋아요 관련 오류 (기존 ExceptionConst.LIKES 카테고리 세분화) ---
    ALREADY_LIKED_POST("L001", HttpStatus.BAD_REQUEST, "이미 좋아요를 누른 게시글입니다. (게시글 ID: {0})"),
    LIKE_NOT_FOUND("L002", HttpStatus.NOT_FOUND, "좋아요 정보를 찾을 수 없습니다."),

    // --- 친구 관련 오류 (기존 ExceptionConst.FRIEND 카테고리 세분화) ---
    FRIEND_REQUEST_NOT_FOUND("FR001", HttpStatus.NOT_FOUND, "해당 친구 요청을 찾을 수 없습니다."), 
    ALREADY_FRIENDS("FR002", HttpStatus.BAD_REQUEST, "이미 친구 관계입니다."),     
    SELF_FRIEND_REQUEST("FR003", HttpStatus.BAD_REQUEST, "자기 자신에게 친구 요청을 보낼 수 없습니다."),       

    // --- 파일 관련 오류 ---
    FILE_UPLOAD_FAILED("F001", HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드 중 오류가 발생했습니다."),
    INVALID_FILE_FORMAT("F002", HttpStatus.BAD_REQUEST, "지원하지 않는 파일 형식입니다. ({0})"),

    ; 
}
