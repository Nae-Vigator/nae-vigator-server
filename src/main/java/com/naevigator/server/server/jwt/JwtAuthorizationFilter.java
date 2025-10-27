package com.naevigator.server.server.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER = "Bearer ";
    private final TokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = resolveToken(request); // 1) 토큰 추출
        if (token != null && tokenProvider.validateToken(token)) { // 2) 검증
            Authentication auth = tokenProvider.getAuthentication(token); // 3) 인증 객체 생성
            SecurityContextHolder.getContext().setAuthentication(auth);  // 4) 컨텍스트 주입
        }
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String value = request.getHeader(AUTH_HEADER);
        if (StringUtils.hasText(value) && value.startsWith(BEARER)) {
            return value.substring(BEARER.length());
        }
        return null;
    }
}
