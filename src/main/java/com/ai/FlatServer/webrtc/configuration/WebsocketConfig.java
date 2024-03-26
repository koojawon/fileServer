package com.ai.FlatServer.webrtc.configuration;

import com.ai.FlatServer.webrtc.controller.PresenterController;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@RequiredArgsConstructor
public class WebsocketConfig implements WebSocketConfigurer {

    private final PresenterController presenterController;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(presenterController, "/audio").setAllowedOrigins("*");
    }
}
