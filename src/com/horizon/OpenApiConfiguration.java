package com.horizon;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {
    @Bean
    OpenAPI horizonOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Horizon Healthcare API")
                .version("0.3.0")
                .description("Patient and encounter management API"));
    }
}
