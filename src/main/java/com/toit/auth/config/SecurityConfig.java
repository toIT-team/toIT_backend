package com.toit.auth.config;

import com.toit.auth.handler.AuthSuccessHandler;
import com.toit.auth.handler.AuthFailureHandler;
import com.toit.auth.jwt.JwtAuthenticationFilter;
import com.toit.auth.service.AppleAuthMemberService;
import com.toit.auth.service.AuthMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthMemberService authMemberService;
    private final AppleAuthMemberService appleAuthMemberService;
    private final AuthSuccessHandler authSuccessHandler;
    private final AuthFailureHandler authFailureHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient;
    private final ClientRegistrationRepository clientRegistrationRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write(
                                    new ObjectMapper().writeValueAsString(Map.of("message", "인증이 필요합니다."))
                            );
                        })
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/admin/login",
                                "/api/admin/register",
                                "/oauth2/**",
                                "/login/oauth2/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/actuator/prometheus",
                                "/actuator/health"
                        ).permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(auth -> auth
                                .authorizationRequestResolver(
                                        new AppleAuthorizationRequestResolver(clientRegistrationRepository))
                        )
                        .tokenEndpoint(token -> token
                                .accessTokenResponseClient(accessTokenResponseClient)
                        )
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(authMemberService) // 카카오
                                .oidcUserService(appleAuthMemberService) // Apple
                        )
                        .failureHandler(authFailureHandler)
                        .successHandler(authSuccessHandler)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // 모바일 앱 및 OAuth 경로 - 모든 origin 허용
        CorsConfiguration mobileConfig = new CorsConfiguration();
        mobileConfig.setAllowedOriginPatterns(List.of("*"));
        mobileConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        mobileConfig.setAllowedHeaders(List.of("*"));
        mobileConfig.setAllowCredentials(true);
        source.registerCorsConfiguration("/api/auth/**", mobileConfig);
        source.registerCorsConfiguration("/oauth2/**", mobileConfig);
        source.registerCorsConfiguration("/login/oauth2/**", mobileConfig);

        // 관리자 페이지
        CorsConfiguration adminConfig = new CorsConfiguration();
        adminConfig.setAllowedOrigins(List.of(
                "https://admin.toit.cloud",
                "http://localhost:3000"
        ));
        adminConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        adminConfig.setAllowedHeaders(List.of("*"));
        adminConfig.setAllowCredentials(true);
        source.registerCorsConfiguration("/api/admin/**", adminConfig);

        // 나머지 API - 모바일 앱에서 호출하므로 모든 origin 허용
        CorsConfiguration apiConfig = new CorsConfiguration();
        apiConfig.setAllowedOriginPatterns(List.of("*"));
        apiConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        apiConfig.setAllowedHeaders(List.of("*"));
        apiConfig.setAllowCredentials(true);
        source.registerCorsConfiguration("/api/**", apiConfig);

        return source;
    }
}
