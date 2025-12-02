package com.spots.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

  @Value("${front.server}")
  private String FRONT_SERVER;

  @Value("${back.server.local}")
  private String BACK_LOCAL_SERVER;

  @Value("${back.server.api}")
  private String BACK_API_SERVER;

  @Value("${back.server.prod}")
  private String BACK_PROD_SERVER;

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/v1/**")
        .allowedOriginPatterns(
            FRONT_SERVER,
            BACK_API_SERVER,
            BACK_PROD_SERVER,
            BACK_LOCAL_SERVER
        )
        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        .allowedHeaders("*")
        .exposedHeaders("Authorization")
        .allowCredentials(true)
        .maxAge(3600);
  }
}