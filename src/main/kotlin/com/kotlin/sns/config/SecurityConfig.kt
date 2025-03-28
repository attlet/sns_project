package com.kotlin.sns.config

import com.kotlin.sns.common.security.CustomAccessDeniedHandler
import com.kotlin.sns.common.security.CustomAuthenticationEntryPoint
import com.kotlin.sns.common.security.JwtAuthenticationFilter
import org.mapstruct.BeanMapping
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
import org.springframework.stereotype.Component

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
    private val customAccessDeniedHandler: CustomAccessDeniedHandler
){
    val permitUrlList = mutableListOf<String>(
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-resources/**",
        "/auth/**",
        "/actuator/**")
    @Bean
    fun filterChain(httpSecurity : HttpSecurity) : SecurityFilterChain {
        httpSecurity
            .csrf{
                it -> it.disable()   //csrf disable
            }
            .cors{
                it -> it.disable()   //cors disable
            }
            .sessionManagement{
                session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)  //서버가 상태를 저장하지 않음, 즉 세션이 x
            }
            .authorizeHttpRequests{
                auth -> auth.requestMatchers(*permitUrlList.toTypedArray()).permitAll()  //인증 없이 접속 가능한 url
                .requestMatchers(HttpMethod.GET, "/postings/**").permitAll()                //posting 을 get하는 api들은 인증 없이 사용 가능
                .requestMatchers(HttpMethod.GET, "/members/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/comment**").permitAll()
                .anyRequest().authenticated()                                            //나머지는 인증 필요한 url
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .exceptionHandling{
                    exception ->
                    exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)


            }

        return httpSecurity.build()
    }

    @Bean
    fun passEncoder() : PasswordEncoder {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder()
    }

}