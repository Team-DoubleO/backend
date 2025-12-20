package com.spots.domain.ai.service;

import static com.spots.global.exception.Code.INVALID_JSON_RESPONSE;
import static com.spots.global.exception.Code.JSON_CONVERSION_ERROR;
import static com.spots.global.exception.Code.LLM_INTERRUPT_ERROR;
import static com.spots.global.exception.Code.PROMPT_LOADING_ERROR;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spots.domain.ai.dto.request.RecommendLLMRequest;
import com.spots.domain.ai.dto.response.WeeklyRecommendResponse;
import com.spots.global.exception.CustomException;
import java.io.InputStream;
import java.util.concurrent.Semaphore;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendLLMService {

  private final ChatClient chatClient;
  private final ObjectMapper objectMapper;
  private final Semaphore llmSemaphore = new Semaphore(5);

  public WeeklyRecommendResponse createWeeklyPlan(RecommendLLMRequest request) {
    try {
      llmSemaphore.acquire();

      String systemMessage = loadPrompt("prompt/routineV2.prompt");
      String userMessage = """
          아래는 사용자 정보와 후보 운동 프로그램 목록입니다.
          이를 기반으로 일주일 운동 루틴 포토카드를 만들 JSON을 생성해주세요.
          
          <user_data>
          %s
          </user_data>
          """.formatted(toJson(request));

      String llmResponse = chatClient
          .prompt()
          .system(systemMessage)
          .user(userMessage)
          .call()
          .content();

      return objectMapper.readValue(sanitize(llmResponse), WeeklyRecommendResponse.class);

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new CustomException(LLM_INTERRUPT_ERROR);
    } catch (JsonProcessingException e) {
      throw new CustomException(INVALID_JSON_RESPONSE);
    } catch (Exception e) {
      throw new CustomException(LLM_INTERRUPT_ERROR);
    } finally {
      llmSemaphore.release();
    }
  }

  private String loadPrompt(String filename) {
    try {
      InputStream in = getClass().getClassLoader().getResourceAsStream(filename);
      return new String(in.readAllBytes(), UTF_8);
    } catch (Exception e) {
      throw new CustomException(PROMPT_LOADING_ERROR);
    }
  }

  private String toJson(Object obj) {
    try {
      return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
    } catch (Exception e) {
      throw new CustomException(JSON_CONVERSION_ERROR);
    }
  }

  private String sanitize(String raw) {
    return raw
        .replaceAll("(?i)```json", "")
        .replaceAll("```", "")
        .trim();
  }
}
