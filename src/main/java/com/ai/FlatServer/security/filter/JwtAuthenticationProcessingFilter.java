package com.ai.FlatServer.security.filter;

import com.ai.FlatServer.security.service.JwtService;
import com.ai.FlatServer.user.repository.UserRepository;
import com.ai.FlatServer.user.repository.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationProcessingFilter extends OncePerRequestFilter {

    private static final String NO_CHECK_URI = "/login";

    private final JwtService jwtService;
    private final UserRepository userRepository;

    private final GrantedAuthoritiesMapper grantedAuthoritiesMapper = new NullAuthoritiesMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        if (request.getRequestURI().equals(NO_CHECK_URI)) {
            filterChain.doFilter(request, response);
            return;
        }
        String refreshToken = jwtService.extractRefreshToken(request)
                .filter(jwtService::isTokenValid)
                .orElse(null);

        if (refreshToken != null) {
            checkRefreshTokenAndReIssueAccessToken(response, refreshToken);
            return;
        }

        checkAccessTokenAndAuthenticate(request, response, filterChain);
    }

    public void checkRefreshTokenAndReIssueAccessToken(HttpServletResponse response, String refreshToken) {
        userRepository.findByRefreshToken(refreshToken)
                .ifPresent(user -> {
                    String reIssuedRefreshToken = jwtService.reIssueRefreshToken(user);
                    jwtService.sendAccessAndRefreshToken(response,
                            jwtService.createAccessToken(user.getEmail()), reIssuedRefreshToken);
                });
    }


    public void checkAccessTokenAndAuthenticate(HttpServletRequest request, HttpServletResponse response,
                                                FilterChain filterChain) throws ServletException, IOException {
        jwtService.extractAccessToken(request)
                .filter(jwtService::isTokenValid)
                .flatMap(jwtService::extractEmail)
                .flatMap(userRepository::findByEmail)
                .ifPresent(this::saveAuthentication);

        filterChain.doFilter(request, response);
    }

    public void saveAuthentication(User user) {
        String password = user.getPassword();
        if (password == null) {
            password = "tempPasswordForSocialUser";
        }

        UserDetails userDetailsUser = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(password)
                .roles(user.getRole().name())
                .build();
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetailsUser, null,
                grantedAuthoritiesMapper.mapAuthorities(userDetailsUser.getAuthorities()));
        authentication.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
