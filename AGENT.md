# AGENT.md

## 1. 프로젝트 개요 (Project Overview)

*   **프로젝트 목표:** Kotlin과 Spring Boot를 기반으로, 확장 가능하고 안정적인 소셜 네트워킹 서비스(SNS)를 구축하는 것을 목표로 합니다. 최신 기술 스택을 활용하여 고성능 및 비동기 처리 기능을 제공합니다.
*   **주요 기능:** 회원 관리, 게시물 작성 및 조회, 친구 관계 관리, 좋아요, 댓글, 해시태그, 이미지 업로드, 실시간 알림 등.

## 2. 핵심 기술 스택 (Core Technology Stack)

프로젝트를 구성하는 주요 기술들을 명시합니다.

*   **언어:** Kotlin 1.9.22
*   **프레임워크:** Spring Boot 3.2.4
*   **빌드 도구:** Gradle
*   **데이터베이스:** MySQL, Redis (캐싱 및 실시간 기능용)
*   **메시지 큐:** Kafka, RabbitMQ (비동기 처리용)
*   **인증:** Spring Security, JWT
*   **API 문서화:** Swagger (Springdoc OpenAPI)
*   **ORM/Query:** Spring Data JPA, Querydsl
*   **비동기 처리:** Spring Async
*   **테스트:** JUnit 5

## 3. 프로젝트 구조 (Project Structure)

주요 디렉토리와 파일의 역할을 설명합니다.

*   `src/main/kotlin/com/kotlin/sns`: 애플리케이션의 메인 소스 코드가 위치합니다.
    *   `common`: 여러 도메인에서 공통으로 사용하는 모듈 (AOP, DTO, 예외 처리, 보안 등)
    *   `config`: 애플리케이션의 주요 설정 파일 (DB, Security, Redis, MQ 등)
    *   `domain`: 비즈니스 로직의 핵심 도메인별로 패키지가 나뉩니다.
        *   `domain/{DomainName}/controller`: API 엔드포인트 담당
        *   `domain/{DomainName}/service`: 비즈니스 로직 처리
        *   `domain/{DomainName}/repository`: 데이터베이스 접근
        *   `domain/{DomainName}/entity`: JPA 엔티티
*   `src/main/resources`: 설정 파일, 정적 리소스가 위치합니다.
    *   `application.yml`: 공통 설정
    *   `application-local.yml`: 로컬 개발 환경 설정
    *   `application-prod.yml`: 프로덕션 환경 설정
*   `src/test`: 테스트 코드가 위치합니다.

## 4. 빌드, 실행 및 테스트 (Build, Run, and Test)

개발에 필수적인 셸 명령어들을 명시합니다.

*   **프로젝트 빌드:**
    ```shell
    ./gradlew build
    ```
*   **애플리케이션 실행 (로컬 환경):**
    ```shell
    ./gradlew bootRun --args='--spring.profiles.active=local'
    ```
*   **전체 테스트 실행:**
    ```shell
    ./gradlew test
    ```
*   **특정 테스트 실행:**
    ```shell
    ./gradlew test --tests "com.kotlin.sns.domain.posting.service.PostingServiceTest"
    ```
