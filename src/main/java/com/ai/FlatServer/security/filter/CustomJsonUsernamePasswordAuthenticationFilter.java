package com.ai.FlatServer.security.filter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StreamUtils;

public class CustomJsonUsernamePasswordAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final String LOGIN_URI = "/login";
    private static final String HTTP_METHOD = "POST";

    private static final String CONTENT_TYPE = "application/json";
    private static final String USERNAME_KEY = "email";
    private static final String PASSWORD_KEY = "password";

    private static final AntPathRequestMatcher LOGIN_PATH_REQUEST_MATCHER = new AntPathRequestMatcher(LOGIN_URI,
            HTTP_METHOD);

    private final ObjectMapper objectMapper;

    public CustomJsonUsernamePasswordAuthenticationFilter(ObjectMapper mapper) {
        super(LOGIN_PATH_REQUEST_MATCHER);
        objectMapper = mapper;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {

        if (request.getContentType() == null || request.getContentType().equals(CONTENT_TYPE)) {
            throw new AuthenticationServiceException("Type not supported" + request.getContentType());
        }

        String messageBody = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
        Map<String, String> usernamePasswordMap = objectMapper.readValue(messageBody,
                new TypeReference<>() {});

        String email = usernamePasswordMap.get(USERNAME_KEY);
        String password = usernamePasswordMap.get(PASSWORD_KEY);

        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(email, password);

        return this.getAuthenticationManager().authenticate(authRequest);
    }
}
