package com.ai.FlatServer.security.handler.jsonlogout;

import com.ai.FlatServer.redis.service.RedisService;
import com.ai.FlatServer.security.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    private final JwtService jwtService;
    private final RedisService redisService;

    @Value("${jwt.access.expiration}")
    private Long accessTokenExpirationPeriod;
    @Value("${jwt.refresh.expiration}")
    private Long refreshTokenExpirationPeriod;

    private void registerExpiredToken(String accessToken, String refreshToken) {
        redisService.setValues(refreshToken, "logout", Duration.ofMillis(refreshTokenExpirationPeriod));
        redisService.setValues(accessToken, "logout", Duration.ofMillis(accessTokenExpirationPeriod));
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                Authentication authentication) throws IOException {
        try {
            String accessToken = jwtService.extractAccessToken(request).orElseThrow();
            String refreshToken = jwtService.extractRefreshToken(request).orElseThrow();
            removeRefreshToken(refreshToken);
            registerExpiredToken(accessToken, refreshToken);
        } catch (NoSuchElementException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("");
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write("logout ok");
    }

    private void removeRefreshToken(String refreshToken) {
        String email = jwtService.extractEmail(refreshToken).orElseThrow();
        redisService.deleteValues(email);
    }
}
