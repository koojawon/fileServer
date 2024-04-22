package com.ai.FlatServer.global.security.configuration;

import com.ai.FlatServer.global.security.filter.JsonUsernamePasswordAuthenticationFilter;
import com.ai.FlatServer.global.security.filter.JwtAuthenticationProcessingFilter;
import com.ai.FlatServer.global.security.handler.jsonlogin.LoginFailureHandler;
import com.ai.FlatServer.global.security.handler.jsonlogin.LoginSuccessHandler;
import com.ai.FlatServer.global.security.handler.jsonlogout.CustomLogoutSuccessHandler;
import com.ai.FlatServer.global.security.handler.oauth2.OAuth2FailureHandler;
import com.ai.FlatServer.global.security.handler.oauth2.OAuth2SuccessHandler;
import com.ai.FlatServer.global.security.service.CustomOAuth2UserService;
import com.ai.FlatServer.global.security.service.LoginService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final LoginService loginService;
    private final JwtAuthenticationProcessingFilter jwtAuthenticationProcessingFilter;
    private final ObjectMapper objectMapper;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final LoginSuccessHandler loginSuccessHandler;
    private final LoginFailureHandler loginFailureHandler;
    private final CustomLogoutSuccessHandler customLogoutSuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .headers(conf -> conf.frameOptions(
                        FrameOptionsConfig::disable))
                .sessionManagement(
                        conf -> conf.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(conf -> conf.requestMatchers("/").permitAll()
                        .requestMatchers(HttpMethod.POST, "/user", "/user/emailCheck").permitAll()
                        .requestMatchers("/audio").permitAll()
                        .anyRequest().authenticated())
                .logout(c -> c.logoutUrl("/logout")
                        .clearAuthentication(true)
                        .logoutSuccessHandler(customLogoutSuccessHandler))
                .oauth2Login(oauth2Login ->
                        oauth2Login.userInfoEndpoint(
                                        userInfoEndpointConfig -> userInfoEndpointConfig.userService(customOAuth2UserService))
                                .successHandler(oAuth2SuccessHandler)
                                .failureHandler(oAuth2FailureHandler)
                )
                .addFilterAfter(jsonUsernamePasswordAuthenticationFilter(), LogoutFilter.class)
                .addFilterBefore(jwtAuthenticationProcessingFilter,
                        JsonUsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        daoAuthenticationProvider.setUserDetailsService(loginService);
        return new ProviderManager(daoAuthenticationProvider);
    }

    @Bean
    public JsonUsernamePasswordAuthenticationFilter jsonUsernamePasswordAuthenticationFilter() {
        JsonUsernamePasswordAuthenticationFilter jsonUsernamePasswordAuthenticationFilter =
                new JsonUsernamePasswordAuthenticationFilter(objectMapper);
        jsonUsernamePasswordAuthenticationFilter.setAuthenticationManager(authenticationManager());
        jsonUsernamePasswordAuthenticationFilter.setAuthenticationSuccessHandler(loginSuccessHandler);
        jsonUsernamePasswordAuthenticationFilter.setAuthenticationFailureHandler(loginFailureHandler);
        return jsonUsernamePasswordAuthenticationFilter;
    }

}
