package com.naevigator.server.server.api;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.Map;

import static com.naevigator.server.server.oauth2.repository.HttpCookieOAuth2AuthorizationRequestRepository.MODE_PARAM_COOKIE_NAME;

@RestController
public class DevController {
    // ✅ 로그인 성공 후 redirect_uri를 여기로 주면 됨
    @GetMapping("/dev/landing")
    public Map<String, Object> landing(@RequestParam(name = "access_token", required = false) String accessToken,
                                       @RequestParam(name = "refresh_token", required = false) String refreshToken
    ) {
        return Map.of(
                "message", "login ok",
                "hasAccessToken", accessToken != null,
                "accessTokenPreview", accessToken != null && accessToken.length() > 20 ? accessToken.substring(0, 20) + "..." : accessToken,
                "refreshToken", refreshToken
        );
    }

    // ✅ 보호 API: JWT가 제대로 들어오면 200, 아니면 401
    @GetMapping("/me")
    public Map<String, Object> me(Authentication auth) {
        return Map.of(
                "name", auth != null ? auth.getName() : "anonymous",
                "authorities", auth != null ? auth.getAuthorities() : null
        );
    }

//    모드 쿠키를 설정하고 OAuth2 인가를 시작하는 임시 엔드포인트
//    사용 예:
//     - /dev/start?provider=kakao&mode=login&redirect=http://localhost:8080/dev/landing
//     - /dev/start?provider=naver&mode=login&redirect=http://localhost:8080/dev/landing

    @GetMapping("/dev/start")
    @ResponseStatus(HttpStatus.FOUND)
    public void start(
            @RequestParam(defaultValue = "kakao") String provider,
            @RequestParam(defaultValue = "login") String mode,
            @RequestParam(defaultValue = "http://localhost:8080/dev/landing") String redirect,
            HttpServletResponse response
    ) {
        // 1) MODE 쿠키 설정 (핸들러가 읽는다)
        Cookie modeCookie = new Cookie(MODE_PARAM_COOKIE_NAME, mode);
        modeCookie.setHttpOnly(true);     // JS가 읽을 필요 없으니 HttpOnly 권장
        modeCookie.setPath("/");          // 전체 경로에 적용
        modeCookie.setMaxAge((int) Duration.ofMinutes(10).getSeconds()); // 짧게
        // 개발환경 http라 Secure는 생략; https면 true + SameSite=None 필요
        response.addCookie(modeCookie);

        // 2) OAuth2 인가 엔드포인트로 리다이렉트 (redirect_uri는 우리가 기대하는 목적지)
        String authorizeUrl = UriComponentsBuilder
                .fromUriString("http://localhost:8080/oauth2/authorization/" + provider)
                .queryParam("redirect_uri", redirect)
                .build(true) // 인코딩
                .toUriString();

        response.setHeader("Location", authorizeUrl);
        response.setStatus(302);
    }

//    MODE 쿠키 제거(초기화)용 – 필요할 때 사용
//    - /dev/clear-mode
    @PostMapping("/dev/clear-mode")
    public Map<String, Object> clearMode(HttpServletResponse response) {
        Cookie c = new Cookie(MODE_PARAM_COOKIE_NAME, "");
        c.setPath("/");
        c.setMaxAge(0); // 삭제
        response.addCookie(c);
        return Map.of("cleared", true);
    }

}
