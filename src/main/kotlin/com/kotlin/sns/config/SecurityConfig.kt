package com.kotlin.sns.config

import com.kotlin.sns.common.security.CustomAccessDeniedHandler
import com.kotlin.sns.common.security.CustomAuthenticationEntryPoint
import com.kotlin.sns.common.security.JwtAuthenticationFilter
import com.kotlin.sns.domain.oauth2.service.CustomOAuth2UserService
import com.kotlin.sns.domain.oauth2.handler.OAuth2AuthenticationSuccessHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

/**
 * security 설정
 *
 * @property jwtAuthenticationFilter
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
class SecurityConfig (
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val customAuthenticationEntryPoint: CustomAuthenticationEntryPoint,
    private val customAccessDeniedHandler: CustomAccessDeniedHandler,
    private val customOAuth2UserService: CustomOAuth2UserService,
    private val oAuth2AuthenticationSuccessHandler: OAuth2AuthenticationSuccessHandler
){
    val permitUrlList = mutableListOf<String>(
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-resources/**",
        "/auth/**",
        "/actuator/**",
        "/login/**",
        "/oauth2/**"
    )
    @Bean
    fun filterChain(httpSecurity : HttpSecurity) : SecurityFilterChain {
        httpSecurity
                        .csrf{
                            it.disable()   //csrf disable
                        }
                        .cors{
                            it.disable()   //cors disable
                        }
                        //서버가 상태를 저장하지 않음, 즉 세션이 x
                        .sessionManagement{
                                session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                        }
                        // 인증 필요없는 url 설정
                        .authorizeHttpRequests{
                                auth -> auth.requestMatchers(*permitUrlList.toTypedArray()).permitAll()  //인증 없이 접속 가능한 url
                            .requestMatchers(HttpMethod.GET, "/postings/**").permitAll()    //posting 을 get하는 api들은 인증 없이 사용 가능
                            .requestMatchers(HttpMethod.GET, "/members/**").permitAll()
                            .requestMatchers(HttpMethod.GET, "/comment**").permitAll()
                            .anyRequest().authenticated()                                            //나머지는 인증 필요한 url
                        }
                        // oauth 인증을 위한 설정
                        .oauth2Login { oauth ->
                            oauth.authorizationEndpoint{
                                it.baseUri("/oauth2/authorize")   //front로부터 OAuth2 인증 요청하는 엔드포인트 URI 설정 ( 설정하지 않으면 기본값 : /oauth2/authorization/{provider} )
                            }
                            oauth.redirectionEndpoint{
                                it.baseUri("/oauth2/callback/*")   //OAuth2 공급자가 인증 후 리디렉션하는 엔드포인트 URI 설정 (설정하지 않으면 기본값 : /login/oauth2/code/{provider} )
                            }
                            oauth.userInfoEndpoint {
                                it.userService(customOAuth2UserService) //OAuth2 인증 과정에서 Authentication 생성에 필요한 OAuth2User 객체를 반환하는 클래스를 지정
                            }
                            oauth.successHandler(oAuth2AuthenticationSuccessHandler)
//                            oauth.failureHandler()
                        }
                        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
                        .exceptionHandling{
                                exception ->
                            exception.authenticationEntryPoint(customAuthenticationEntryPoint)
                                .accessDeniedHandler(customAccessDeniedHandler)
            }

        return httpSecurity.build()
    }


    @Bean
    fun passEncoder() : PasswordEncoder {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder()
    }

}