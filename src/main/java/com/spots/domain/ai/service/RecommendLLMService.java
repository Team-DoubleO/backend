package com.spots.domain.ai.service;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spots.domain.ai.dto.request.RecommendLLMRequest;
import com.spots.domain.ai.dto.response.WeeklyRecommendResponse;
import java.io.InputStream;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecommendLLMService {

  private final ChatClient chatClient;
  private final ObjectMapper objectMapper;

  public WeeklyRecommendResponse createWeeklyPlan(RecommendLLMRequest request) {

    String promptText = loadPrompt("prompt/routine.prompt");

    String userMessage = """
        아래는 사용자 정보와 후보 운동 프로그램 목록입니다.
        이를 기반으로 일주일 운동 루틴 포토카드를 만들 JSON을 생성해주세요.
        
        <user_data>
        %s
        </user_data>
        """.formatted(toJson(request));

    Prompt prompt = new Prompt(
        List.of(
            new SystemMessage(promptText),
            new UserMessage(userMessage)
        )
    );

    var llmResponse = chatClient
        .prompt(prompt)
        .system(promptText)
        .user(userMessage)
        .call()
        .content();

    try {
      return objectMapper.readValue(llmResponse, WeeklyRecommendResponse.class);
    } catch (Exception e) {
      throw new RuntimeException("LLM 응답 파싱 실패: " + llmResponse);
    }
  }

  private String loadPrompt(String filename) {
    try {
      InputStream in = getClass().getClassLoader().getResourceAsStream(filename);
      return new String(in.readAllBytes(), UTF_8);
    } catch (Exception e) {
      throw new RuntimeException("프롬프트 파일 로드 실패", e);
    }
  }

  private String toJson(Object obj) {
    try {
      return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
    } catch (Exception e) {
      throw new RuntimeException("JSON 변환 실패", e);
    }
  }
}
