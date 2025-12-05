package com.spots.domain.ai;

import com.spots.domain.ai.client.GmsOpenAiClient;
import com.spots.domain.ai.dto.response.GmsChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GmsOpenAiController {

  private final GmsOpenAiClient gmsOpenAiClient;

  @GetMapping("/ai/ask")
  public String ask(@RequestParam String userInput) {
    System.out.println("userInput = " + userInput);

    GmsChatResponse response = gmsOpenAiClient
        .chat(userInput)
        .block();

    return response.content();
  }
}