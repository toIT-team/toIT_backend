package com.toit.auth.config;

import com.toit.auth.handler.AuthSuccessHandler;
import com.toit.auth.handler.AuthFailureHandler;
import com.toit.auth.jwt.JwtAuthenticationFilter;
import com.toit.auth.service.AuthMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthMemberService authMemberService;
    private final AuthSuccessHandler authSuccessHandler;
    private final AuthFailureHandler authFailureHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter; // 3. 필터 주입 추가

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. CSRF 및 세션 비활성화 (JWT를 사용할 것이므로)
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )

                // 2. 경로별 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/reissue").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/auth/social/kakao/login").permitAll()
                        .requestMatchers("/", "/login/**", "/oauth2/**").permitAll() // 로그인 관련 경로는 모두 허용
                        .anyRequest().authenticated() // 그 외 나머지는 인증 필요
                )

                // 3. OAuth2 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(authMemberService) // 우리가 만든 서비스 등록
                        )
                        .failureHandler(authFailureHandler)
                        .successHandler(authSuccessHandler) // 우리가 만든 핸들러 등록
                ).addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
