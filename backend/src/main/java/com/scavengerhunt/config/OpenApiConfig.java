package com.scavengerhunt.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI scavengerHuntOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Scavenger Hunt Game API")
                        .version("v1.0")
                        .description("RESTful API for the Scavenger Hunt location-based game")
                        .contact(new Contact()
                                .name("Scavenger Hunt Team")
                                .email("wyatt_h.w@protonmail.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8443").description("Local Development")
                ));
    }
}