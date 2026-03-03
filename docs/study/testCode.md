

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


# ReviewServiceTest에서 사용된 기술 (MockK 기반 단위 테스트)

> PostingServiceTest(Mockito + @SpringBootTest)와 달리, ReviewServiceTest는
> MockK + JUnit5 Extension만으로 Spring 컨텍스트 없이 순수 단위 테스트를 작성한다.

-----------------------------------------------------------------------------
1. @ExtendWith(MockKExtension::class)
-----------------------------------------------------------------------------
- MockK를 JUnit 5와 연동하는 확장
- @SpringBootTest 없이 사용 → Spring 컨텍스트를 로드하지 않음
- 테스트 클래스 시작 속도가 압도적으로 빠름

```kotlin
// PostingServiceTest (느림 - Spring 컨텍스트 전체 로드)
@SpringBootTest
class PostingServiceImplTest { ... }

// ReviewServiceTest (빠름 - 컨텍스트 없음)
@ExtendWith(MockKExtension::class)
class ReviewServiceTest { ... }
```

-----------------------------------------------------------------------------
2. mockk() - Mock 객체 생성
-----------------------------------------------------------------------------
- MockK의 기본 mock 생성 함수
- Mockito의 `mock()` / `@MockBean`에 대응

### 일반 mockk()
- 명시적으로 stubbing하지 않은 메서드를 호출하면 예외 발생
- 테스트가 의도치 않은 호출을 놓치지 않게 강제함

```kotlin
val reviewRepository: ReviewRepository = mockk()
```

### mockk(relaxed = true)
- stubbing하지 않은 메서드도 기본값 반환 (0, false, null, 빈 문자열 등)
- 반환값이 중요하지 않은 의존 객체(entity mock 등)에 사용

```kotlin
// entity의 모든 프로퍼티를 일일이 stubbing하지 않아도 됨
val review: Review = mockk(relaxed = true)
val member: Member = mockk(relaxed = true)
```

| | 일반 mockk() | mockk(relaxed = true) |
|---|---|---|
| 미stubbing 호출 시 | 예외 발생 | 기본값 반환 |
| 사용 대상 | 핵심 의존성(Repository 등) | 보조 객체(entity mock 등) |

-----------------------------------------------------------------------------
3. mockkObject() - Kotlin object(싱글톤) mocking
-----------------------------------------------------------------------------
- Kotlin의 `object` 키워드로 선언된 싱글톤을 mocking할 때 사용
- Mockito로는 불가능한 영역 (static/object mocking은 별도 라이브러리 필요)
- `@BeforeEach`에서 호출하여 매 테스트 전에 초기화

```kotlin
// ReviewMapper는 object(싱글톤)로 선언된 MapStruct 매퍼
@BeforeEach
fun setUp() {
    reviewService = ReviewServiceImpl(reviewRepository, memberRepository, contentRepository)
    mockkObject(ReviewMapper)  // object를 mock으로 교체
}
```

```kotlin
// 이후 every { }로 object의 메서드를 stubbing 가능
every { ReviewMapper.toDto(review) } returns responseDto
every { ReviewMapper.toEntity(request, member, content) } returns review
```

-----------------------------------------------------------------------------
4. every { } returns - Stubbing DSL
-----------------------------------------------------------------------------
- Mockito의 `whenever(...).thenReturn(...)`에 대응하는 MockK 문법
- 람다 블록 안에서 호출을 정의하는 DSL 스타일

```kotlin
// Mockito 방식
whenever(reviewRepository.findById(id)).thenReturn(Optional.of(review))

// MockK 방식 (Kotlin 친화적)
every { reviewRepository.findActiveById(id) } returns review
```

### 예외 발생 stubbing
```kotlin
// Mockito 방식
whenever(repo.findById(id)).thenThrow(RuntimeException())

// MockK 방식
every { repo.findById(id) } throws RuntimeException()
```

### Unit 반환 stubbing (반환값이 없는 함수)
```kotlin
// just Runs: Unit 반환 메서드를 아무 동작 없이 처리
every { notificationService.send(any()) } just Runs
```

-----------------------------------------------------------------------------
5. verify { } - 호출 검증
-----------------------------------------------------------------------------
- 메서드가 실제로 호출됐는지 검증
- Mockito의 `Mockito.verify()`에 대응
- `exactly`로 호출 횟수를 정확하게 명시할 수 있어 더 엄밀한 검증 가능

```kotlin
// Mockito 방식 (기본 1회)
Mockito.verify(postingRepository).deleteById(postingId)

// MockK 방식 (호출 횟수 명시)
verify(exactly = 1) { reviewRepository.findActiveById(reviewId) }
verify(exactly = 0) { reviewRepository.save(any()) }  // 호출되지 않아야 함
```

| 옵션 | 의미 |
|---|---|
| `exactly = 1` | 정확히 1번 호출 |
| `exactly = 0` | 한 번도 호출되지 않아야 함 |
| `atLeast = 1` | 최소 1번 이상 |
| `atMost = 2` | 최대 2번 이하 |

-----------------------------------------------------------------------------
6. 생성자 직접 주입 vs @Autowired
-----------------------------------------------------------------------------
- PostingServiceTest는 `@Autowired`로 Spring이 주입해준 Bean을 사용
- ReviewServiceTest는 생성자로 mock을 직접 주입 → Spring 불필요

```kotlin
// PostingServiceTest - Spring 주입
@Autowired
private lateinit var postingService: PostingServiceImpl

// ReviewServiceTest - 직접 주입
private val reviewRepository: ReviewRepository = mockk()

@BeforeEach
fun setUp() {
    reviewService = ReviewServiceImpl(reviewRepository, memberRepository, contentRepository)
}
```

직접 주입 방식의 장점:
- 어떤 의존성이 주입되는지 테스트 코드에서 명확히 보임
- Spring 컨텍스트 없이 동작 → 테스트가 빠름
- 생성자 주입을 강제하므로 프로덕션 코드의 설계도 개선됨

-----------------------------------------------------------------------------
7. ErrorCode 직접 비교 vs 메시지 문자열 비교
-----------------------------------------------------------------------------

```kotlin
// PostingServiceTest - 메시지 문자열 비교 (취약)
assertThat(exception.message).contains(ErrorCode.POST_NOT_FOUND.message)
// ❌ ErrorCode.message 값이 바뀌면 테스트도 깨짐
// ❌ exception.message 형식에 의존적

// ReviewServiceTest - ErrorCode 열거형 직접 비교 (강건)
assertEquals(ErrorCode.REVIEW_NOT_FOUND, exception.errorCode)
// ✅ 메시지 변경과 무관하게 올바른 에러코드인지 검증
```

=============================================================================
                    정리: MockK vs Mockito 선택 기준
=============================================================================

| 상황 | 선택 |
|---|---|
| Kotlin object / companion object mocking | MockK (Mockito 불가) |
| Spring @MockBean이 필요한 통합 테스트 | Mockito |
| 순수 단위 테스트, 빠른 속도 원할 때 | MockK |
| 호출 횟수를 정확히 검증해야 할 때 | MockK (exactly 옵션) |
| 기존 Java 코드베이스와 공존 | Mockito |

=============================================================================
