package com.ai.FlatServer.security.handler.oauth2;

import com.ai.FlatServer.security.dto.oauth2.CustomOAuth2User;
import com.ai.FlatServer.security.service.JwtService;
import com.ai.FlatServer.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        loginSuccess(request, response, customOAuth2User);
    }

    private void loginSuccess(HttpServletRequest request, HttpServletResponse response,
                              CustomOAuth2User customOAuth2User) {
        String accessToken = jwtService.createAccessToken(customOAuth2User.getEmail());
        String refreshToken = jwtService.createRefreshToken(customOAuth2User.getEmail());
        request.getHeader(jwtService.getRefreshHeader());

        response.addHeader(jwtService.getAccessHeader(), "Bearer " + accessToken);
        response.addHeader(jwtService.getRefreshHeader(), "Bearer " + refreshToken);

        jwtService.sendAccessAndRefreshToken(response, accessToken, refreshToken);
        jwtService.updateRefreshTokenOfRepository(customOAuth2User.getEmail(), refreshToken);
    }
}
