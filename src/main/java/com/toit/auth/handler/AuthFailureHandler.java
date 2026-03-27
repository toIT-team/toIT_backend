package com.toit.auth.handler;

import com.toit.auth.login.SocialLoginResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class AuthFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Value("${toit.auth.app-callback-uri:toit://auth/callback}")
    private String appCallbackUri;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        SocialLoginResult result = resolveResult(exception);
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(appCallbackUri)
                .queryParam("result", result.getQueryValue());

        if (result == SocialLoginResult.FAILED) {
            builder.queryParam("errorCode", resolveErrorCode(exception));
        }

        UriComponents targetUrl = builder.build().encode(java.nio.charset.StandardCharsets.UTF_8);
        getRedirectStrategy().sendRedirect(request, response, targetUrl.toUriString());
    }

    private SocialLoginResult resolveResult(AuthenticationException exception) {
        if (exception instanceof OAuth2AuthenticationException oauth2Exception
                && "access_denied".equalsIgnoreCase(oauth2Exception.getError().getErrorCode())) {
            return SocialLoginResult.CANCELLED;
        }
        return SocialLoginResult.FAILED;
    }

    private String resolveErrorCode(AuthenticationException exception) {
        if (exception instanceof OAuth2AuthenticationException oauth2Exception) {
            return oauth2Exception.getError().getErrorCode().toUpperCase();
        }
        return "AUTH_SERVER_ERROR";
    }
}
