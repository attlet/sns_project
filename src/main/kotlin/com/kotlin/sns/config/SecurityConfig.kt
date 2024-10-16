package com.kotlin.sns.config

import com.kotlin.sns.common.security.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
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
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
){

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
                auth -> auth.requestMatchers("/**").permitAll()  //인증 없이 접속 가능한 url
                .anyRequest().authenticated()                            //나머지는 인증 필요한 url
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return httpSecurity.build()
    }
}