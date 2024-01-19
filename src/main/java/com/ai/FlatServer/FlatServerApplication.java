package com.ai.FlatServer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
//@EnableWebSocket
@EnableJpaAuditing
public class FlatServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlatServerApplication.class, args);
    }

//    @Bean
//    public CallHandler callHandler() {
//        return new CallHandler();
//    }

//    @Bean
//    public UserRegistry registry() {
//        return new UserRegistry();
//    }

//    @Bean
//    public KurentoClient kurentoClient() {
//        return KurentoClient.create();
//    }

//    @Bean
//    public ServletServerContainerFactoryBean createServletServerContainerFactoryBean() {
//        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
//        container.setMaxTextMessageBufferSize(32768);
//        return container;
//    }
//
//    @Override
//    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
//        registry.addHandler(callHandler(), "/audio").setAllowedOrigins("*");
//    }
}
