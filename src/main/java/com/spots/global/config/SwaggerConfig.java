package com.spots.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  @Value("${swagger.server-url}")
  private String SERVER_URL;

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
        .servers(List.of(
            new Server().url(SERVER_URL)
        )).info(info);
  }

  Info info = new Info()
      .title("KSPO Pulbic Data Competition API")
      .version("0.0.1")
      .description("<h3>KSPO Public Data Competition</h3>");
}
