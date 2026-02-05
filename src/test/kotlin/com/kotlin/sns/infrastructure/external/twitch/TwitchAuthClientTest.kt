package com.kotlin.sns.infrastructure.external.twitch

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.kotlin.sns.common.exception.CustomException
import com.kotlin.sns.common.exception.ErrorCode
import com.kotlin.sns.infrastructure.external.twitch.Impl.TwitchAuthClientImpl
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.springframework.web.reactive.function.client.WebClient
import java.time.Instant

/**
 * TwitchAuthClient 테스트
 *
 * WireMock을 사용하여 Twitch OAuth API를 모킹합니다.
 * 실제 Twitch 서버에 연결하지 않고 다양한 시나리오를 테스트합니다.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("TwitchAuthClient 테스트")
class TwitchAuthClientTest {

    private lateinit var wireMockServer: WireMockServer
    private lateinit var twitchAuthClient: TwitchAuthClient
    private lateinit var webClient: WebClient

    companion object {
        private const val TEST_CLIENT_ID = "test-client-id"
        private const val TEST_CLIENT_SECRET = "test-client-secret"
        private const val TOKEN_ENDPOINT = "/oauth2/token"
    }

    /**
     * 테스트 코드 실행 전, WireMock 서버 구동
     */
    @BeforeAll
    fun setUpWireMock() {
        wireMockServer = WireMockServer(wireMockConfig().dynamicPort())
        wireMockServer.start()
    }

    /**
     * 테스트 코드 실행 후, WireMock 서버 종료
     */
    @AfterAll
    fun tearDown() {
        wireMockServer.stop()
    }

    /**
     * 각 테스트 시작 전, 초기화
     */
    @BeforeEach
    fun setUp() {
        // 이전 테스트 stub 제거
        wireMockServer.resetAll()

        // WebClient 초기화
        webClient = WebClient.builder()
            .baseUrl("http://localhost:${wireMockServer.port()}")
            .build()

        // twithAuthClient 초기화
        twitchAuthClient = TwitchAuthClientImpl(
            webClient = webClient,
            clientId = TEST_CLIENT_ID,
            clientSecret = TEST_CLIENT_SECRET
        )
    }

    @Nested
    @DisplayName("getAccessToken 테스트")
    inner class GetAccessTokenTest {

        @Test
        @DisplayName("유효한 credentials로 토큰 발급 성공")
        fun `given valid credentials when getAccessToken then return token response`() {
            // Given
            stubResponse(200, """
                {
                    "access_token": "test-access-token-12345",
                    "expires_in": 3600,
                    "token_type": "bearer"
                }
            """.trimIndent())

            // When
            val result = twitchAuthClient.getAccessToken()

            // Then - 요청 값 검증
            wireMockServer.verify(
                postRequestedFor(urlEqualTo(TOKEN_ENDPOINT))
                    .withRequestBody(containing("client_id=$TEST_CLIENT_ID"))
                    .withRequestBody(containing("client_secret=$TEST_CLIENT_SECRET"))
                    .withRequestBody(containing("grant_type=client_credentials"))
            )

            // Then - 응답 검증
            assertAll(
                { assertNotNull(result) },
                { assertEquals("test-access-token-12345", result.accessToken) },
                { assertEquals(3600L, result.expiresIn) },
                { assertEquals("bearer", result.tokenType) },
                { assertTrue(result.expiresAt().isAfter(Instant.now())) }
            )
        }

        @Test
        @DisplayName("잘못된 Client ID로 401 에러 발생")
        fun `given invalid client id when getAccessToken then throw CustomException with TWITCH_AUTH_FAILED`() {
            // Given
            stubResponse(401, """{"status": 401, "message": "invalid client"}""")

            // When & Then
            val exception = assertThrows<CustomException> {
                twitchAuthClient.getAccessToken()
            }

            assertEquals(ErrorCode.TWITCH_AUTH_FAILED, exception.errorCode)
        }

        @Test
        @DisplayName("잘못된 Client Secret으로 403 에러 발생")
        fun `given invalid client secret when getAccessToken then throw CustomException with TWITCH_AUTH_FAILED`() {
            // Given
            stubResponse(403, """{"status": 403, "message": "invalid client secret"}""")

            // When & Then
            val exception = assertThrows<CustomException> {
                twitchAuthClient.getAccessToken()
            }

            assertEquals(ErrorCode.TWITCH_AUTH_FAILED, exception.errorCode)
        }

        @Test
        @DisplayName("서버 오류 시 예외 발생")
        fun `given server error when getAccessToken then throw CustomException with TWITCH_AUTH_SERVER_ERROR`() {
            // Given
            stubResponse(500, "Internal Server Error")

            // When & Then
            val exception = assertThrows<CustomException> {
                twitchAuthClient.getAccessToken()
            }

            assertEquals(ErrorCode.TWITCH_AUTH_SERVER_ERROR, exception.errorCode)
        }
    }

    @Nested
    @DisplayName("getValidToken 테스트")
    inner class GetValidTokenTest {

        @Test
        @DisplayName("캐시된 토큰이 없으면 새 토큰 발급")
        fun `given no cached token when getValidToken then fetch new token`() {
            // Given
            stubResponse(200, """
                {
                    "access_token": "new-token-abc",
                    "expires_in": 3600,
                    "token_type": "bearer"
                }
            """.trimIndent())

            // When
            val token = twitchAuthClient.getValidToken()

            // Then
            assertEquals("new-token-abc", token)
            wireMockServer.verify(1, postRequestedFor(urlEqualTo(TOKEN_ENDPOINT)))
        }

        @Test
        @DisplayName("유효한 캐시 토큰이 있으면 재사용 (API 호출 없음)")
        fun `given valid cached token when getValidToken then return cached token without api call`() {
            // Given
            stubResponse(200, """
                {
                    "access_token": "cached-token-xyz",
                    "expires_in": 3600,
                    "token_type": "bearer"
                }
            """.trimIndent())
            val firstToken = twitchAuthClient.getValidToken()

            // When
            val secondToken = twitchAuthClient.getValidToken()

            // Then
            assertAll(
                { assertEquals(firstToken, secondToken) },
                { assertEquals("cached-token-xyz", secondToken) }
            )
            wireMockServer.verify(1, postRequestedFor(urlEqualTo(TOKEN_ENDPOINT)))
        }

        @Test
        @DisplayName("여러 번 호출해도 캐시된 토큰 재사용")
        fun `given cached token when getValidToken called multiple times then reuse cached token`() {
            // Given
            stubResponse(200, """
                {
                    "access_token": "cached-token",
                    "expires_in": 3600,
                    "token_type": "bearer"
                }
            """.trimIndent())

            // When
            repeat(5) { twitchAuthClient.getValidToken() }

            // Then
            wireMockServer.verify(1, postRequestedFor(urlEqualTo(TOKEN_ENDPOINT)))
        }
    }

    /**
     * wireMockServer 반환 정보 세팅하는 함수
     *
     * @param status : 예상 http 상태 코드
     * @param body   : 예상 json body
     */
    private fun stubResponse(status : Int, body : String = "") {
        wireMockServer.stubFor(
            post(urlEqualTo(TOKEN_ENDPOINT))
                .willReturn(
                    aResponse()
                        .withStatus(status)
                        .withHeader("Content-Type", "application/json")
                        .withBody(body)
                )
        )
    }
}
