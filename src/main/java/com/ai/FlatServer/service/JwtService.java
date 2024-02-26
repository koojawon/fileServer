package com.ai.FlatServer.service;

import com.ai.FlatServer.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Getter
@Slf4j
public class JwtService {

    private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
    private static final String REFRESH_TOKEN_SUBJECT = "RefreshToken";
    private static final String EMAIL_CLAIM = "eml";
    private static final String BEARER = "Bearer";
    private final UserRepository userRepository;
    @Value("${jwt.secretKey}")
    private final String secretKey;
    private SecretKey encodedKey;
    @Value("{jwt.access.expiration}")
    private Long accessTokenExpirationPeriod;
    @Value("{jwt.refresh.expiration}")
    private Long refreshTokenExpirationPeriod;
    @Value("{jwt.access.header}")
    private String accessHeader;
    @Value("{jwt.refresh.header}")
    private String refreshHeader;

    @PostConstruct
    public void initKey() {
        this.encodedKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }

    public String createAccessToken(String email) {
        return Jwts.builder()
                .subject(ACCESS_TOKEN_SUBJECT)
                .expiration(Date.from(Instant.now()
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .plusMillis(accessTokenExpirationPeriod)))
                .claim(EMAIL_CLAIM, email)
                .signWith(encodedKey)
                .compact();
    }

    public String createRefreshToken() {
        return Jwts.builder()
                .subject(REFRESH_TOKEN_SUBJECT)
                .expiration(Date.from(Instant.now()
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .plusMillis(refreshTokenExpirationPeriod)))
                .signWith(encodedKey)
                .compact();
    }

    public void sendAccessToken(HttpServletResponse response, String accessToken) {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader(accessHeader, accessToken);
    }

    public void sendAccessAndRefreshToken(HttpServletResponse response, String accessToken, String refreshToken) {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader(accessHeader, accessToken);
        response.setHeader(refreshHeader, refreshToken);
    }

    public Optional<String> extractRefreshToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(refreshHeader))
                .filter(t -> t.startsWith(BEARER))
                .map(t -> t.replace(BEARER, ""));
    }

    public Optional<String> extractAccessToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(accessHeader))
                .filter(t -> t.startsWith(BEARER))
                .map(t -> t.replace(BEARER, ""));
    }

    public Optional<String> extractEmail(String accessToken) {
        try {
            return Optional.ofNullable(Jwts.parser()
                    .verifyWith(encodedKey)
                    .build()
                    .parseSignedClaims(accessToken)
                    .getPayload()
                    .get(EMAIL_CLAIM)
                    .toString());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public void setAccessHeader(HttpServletResponse response, String accessToken) {
        response.setHeader(accessHeader, accessToken);
    }

    public void setRefreshHeader(HttpServletResponse response, String refreshToken) {
        response.setHeader(refreshHeader, refreshToken);
    }

    public void updateRefreshTokenOfRepository(String email, String refreshToken) throws Exception {
        userRepository.findByEmail(email)
                .orElseThrow(Exception::new)
                .updateRefreshToken(refreshToken);
    }

    public boolean isTokenValid(String accessToken) {
        try {
            Jwts.parser()
                    .verifyWith(encodedKey)
                    .build();
            return true;
        } catch (SignatureException e) {
            return false;
        }
    }
}
