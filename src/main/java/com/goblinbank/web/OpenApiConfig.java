package com.goblinbank.web;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI openAPI() {
    final String name = "bearerAuth";
    return new OpenAPI()
        .info(new Info().title("Goblin Bank API").version("1.0"))
        .components(
            new Components()
                .addSecuritySchemes(
                    name,
                    new SecurityScheme()
                        .name(name)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
  }
}
