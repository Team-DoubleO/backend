package com.spots.domain.ai.client;

import com.spots.domain.ai.dto.response.GmsChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class GmsOpenAiClient {

  private final WebClient webClient;

  @Value("${gms.base-url}")
  private String GMS_BASE_URL;

  public GmsOpenAiClient(@Value("${gms.api-key}") String key) {
    this.webClient = WebClient.builder()
        .baseUrl(GMS_BASE_URL)
        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + key)
        .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
        .build();
  }

  public Mono<GmsChatResponse> chat(String prompt) {
    return webClient.post()
        .uri("/v1/chat/completions")
        .bodyValue("""
            {
              "model": "gpt-5",
              "messages": [
                { "role": "developer", "content": "Answer in Korean" },
                { "role": "user", "content": "%s" }
              ]
            }
            """.formatted(prompt))
        .retrieve()
        .bodyToMono(GmsChatResponse.class);
  }
}
