

# TwitchAuthClientTest에서 사용된 기술

## 1. WireMock (HTTP Mock Server)
- 라이브러리: com.github.tomakehurst.wiremock
- 용도: 외부 HTTP API를 모킹하여 실제 서버 없이 테스트 수행

### 주요 컴포넌트
- WireMockServer: 가짜 HTTP 서버 인스턴스
- wireMockConfig().dynamicPort(): 사용 가능한 포트를 동적으로 할당
- stubFor(): 특정 요청에 대한 응답 정의
- verify(): API 호출이 예상대로 발생했는지 검증

### 사전 set up
```kotlin
@BeforeAll
public setup(){
    wireMockServer = WireMockServer(wireMockConfig.dynamicPort()) // 
    wireMockServer.start()
}

```
- dynamicPort 설정을 통해 os 가 빈 포트를 테스트 시 자동으로 할당
- 병렬 테스트 안전하게 하기 위함

### 사용 예시
```kotlin
wireMockServer.stubFor(
    post(urlEqualTo("/oauth2/token"))
        .withRequestBody(containing("client_id=..."))
        .willReturn(
            aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("""{"access_token": "..."}""")
        )
)
```

### 장점
- 외부 API 의존성 제거 (네트워크 불필요)
- 다양한 시나리오 테스트 가능 (성공, 실패, 타임아웃 등)
- 테스트 속도 향상
- 요청/응답 검증 가능

-----------------------------------------------------------------------------
2. JUnit 5 어노테이션
-----------------------------------------------------------------------------

### @TestInstance(TestInstance.Lifecycle.PER_CLASS)
- 테스트 클래스당 하나의 인스턴스만 생성
- @BeforeAll/@AfterAll 메서드를 non-static으로 선언 가능
- WireMock 서버를 클래스 전체에서 공유할 때 유용

### @DisplayName
- 테스트 결과에 표시되는 이름을 한글 등으로 지정
- 가독성 향상: `@DisplayName("유효한 credentials로 토큰 발급 성공")`

### @Nested
- 테스트를 논리적으로 그룹화하는 내부 클래스
- 테스트 구조를 계층적으로 구성
- 예: GetAccessTokenTest, GetValidTokenTest로 분리

### 생명주기 어노테이션
- @BeforeAll: 모든 테스트 전 1회 실행 (서버 시작)
- @AfterAll: 모든 테스트 후 1회 실행 (서버 종료)
- @BeforeEach: 각 테스트 전 실행 (상태 초기화)

-----------------------------------------------------------------------------
3. JUnit 5 Assertions
-----------------------------------------------------------------------------

### assertAll()
- 여러 assertion을 그룹으로 실행
- 하나가 실패해도 나머지 모두 실행 후 결과 종합
```kotlin
assertAll(
    { assertNotNull(result) },
    { assertEquals("expected", result.value) },
    { assertTrue(result.isValid) }
)
```

### assertThrows<T>
- 예외 발생을 검증하는 Kotlin 확장 함수
- 발생한 예외 인스턴스를 반환하여 추가 검증 가능
```kotlin
val exception = assertThrows<TwitchAuthException> {
    twitchAuthClient.getAccessToken()
}
assertTrue(exception.message?.contains("401") == true)
```

-----------------------------------------------------------------------------
4. Spring WebClient
-----------------------------------------------------------------------------
- Spring WebFlux의 비동기/논블로킹 HTTP 클라이언트
- 테스트에서 baseUrl을 WireMock 서버로 지정하여 모킹

```kotlin
webClient = WebClient.builder()
    .baseUrl("http://localhost:${wireMockServer.port()}")
    .build()
```

-----------------------------------------------------------------------------
5. Given-When-Then 패턴 (BDD 스타일)
-----------------------------------------------------------------------------
- Given: 테스트 사전 조건 설정
- When: 테스트 대상 동작 수행
- Then: 결과 검증

### 메서드 네이밍 컨벤션
```kotlin
@Test
fun `given valid credentials when getAccessToken then return token response`()
```
- 백틱(`)을 사용한 Kotlin 메서드명으로 가독성 높은 테스트명 작성

-----------------------------------------------------------------------------
6. companion object
-----------------------------------------------------------------------------
- Kotlin의 정적 상수 정의 방식
- 테스트 전체에서 사용되는 상수값 관리

```kotlin
companion object {
    private const val TEST_CLIENT_ID = "test-client-id"
    private const val TEST_CLIENT_SECRET = "test-client-secret"
}
```

=============================================================================
                        정리: 테스트 작성 패턴
=============================================================================

1. 외부 API 테스트 시 WireMock으로 모킹
2. @Nested로 테스트를 기능별로 그룹화
3. Given-When-Then으로 테스트 구조 명확화
4. @DisplayName으로 한글 설명 추가
5. assertAll로 여러 검증을 한 번에 수행
6. @TestInstance(PER_CLASS)로 비용이 큰 리소스(서버) 공유

=============================================================================
