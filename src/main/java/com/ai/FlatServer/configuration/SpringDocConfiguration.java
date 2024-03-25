package com.ai.FlatServer.configuration;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfiguration {

    @Bean
    public OpenAPI apiV1() {
        return new OpenAPI()
                .info(new Info().title("Flat API")
                        .description("API for Flat dev")
                        .version("0.1.1")
                );
    }
}
