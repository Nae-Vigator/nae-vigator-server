package com.naevigator.server.server.oauth2.handler;

import com.naevigator.server.server.oauth2.repository.HttpCookieOAuth2AuthorizationRequestRepository;
import com.naevigator.server.server.oauth2.util.CookieUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

import static com.naevigator.server.server.oauth2.repository.HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME;

@Slf4j
@RequiredArgsConstructor
@Component
public class OAuth2AuthenticationFailureHandler  extends SimpleUrlAuthenticationFailureHandler {

    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        // 1) 리다이렉트 목적지 복원 (없으면 기본값)
        String target = CookieUtils.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)
                .map(Cookie::getValue)
                .orElse(("/"));

        // 2) 상세 메시지는 로그에
        log.warn("OAuth2 login failed: {}", exception.getMessage(), exception);

        // 3) 클라이언트에는 URL-세이프하게 인코딩된 에러 메시지 전달
        //   build(true) 를 써야 쿼리 파라미터가 RFC3986에 맞게 인코딩됨
        String encoded = exception.getMessage() == null ? "unknown_error" : exception.getMessage();

        String redirect = UriComponentsBuilder.fromUriString(target)
                .queryParam("error", encoded)   // 값은 자동 인코딩됨
                .build(true)                    // ★ 중요: encoding on
                .toUriString();

        // 4) 사용한 쿠키/상태 정리
        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);

        // 5) 리다이렉트
        getRedirectStrategy().sendRedirect(request, response, target);
    }
}
