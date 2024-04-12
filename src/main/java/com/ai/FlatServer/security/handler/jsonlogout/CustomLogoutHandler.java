package com.ai.FlatServer.security.handler.jsonlogout;

import com.ai.FlatServer.security.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {

    private final JwtService jwtService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String accessToken = jwtService.extractAccessToken(request).orElseThrow();
        String refreshToken = jwtService.extractRefreshToken(request).orElseThrow();
        registerExpiredToken(accessToken, refreshToken);
    }

    private void registerExpiredToken(String accessToken, String refreshToken) {
        //put tokens into redis!!
    }
}
