package com.toit.auth.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 요청 헤더에서 JWT 토큰을 추출합니다.
        String token = parseBearerToken(request);

        // 2. 토큰이 유효하다면 유저 정보를 꺼내 시큐리티 컨텍스트에 담습니다.
        if (StringUtils.hasText(token) && jwtProvider.validateToken(token)) {

            //토큰에서 ROLE과 USERID를 꺼냄
            String userId = jwtProvider.getUserId(token);
            String role = jwtProvider.getRole(token);

            // 시큐리티 내부에서 사용할 인증 객체를 생성합니다.
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userId, null, Collections.singleton(new SimpleGrantedAuthority(role)) // 꺼낸 권한 적용
            );

            // 유저가 인증되면 넘어갑니다
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 3. 다음 필터로 요청을 넘깁니다.
        filterChain.doFilter(request, response);
    }

    private String parseBearerToken(HttpServletRequest request) {
        // 1. 모바일 앱 등: Authorization 헤더의 Bearer 토큰
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        // 2. 관리자 웹: httpOnly 쿠키의 토큰
        return parseCookieToken(request);
    }

    private String parseCookieToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if ("admin_token".equals(cookie.getName()) && StringUtils.hasText(cookie.getValue())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}