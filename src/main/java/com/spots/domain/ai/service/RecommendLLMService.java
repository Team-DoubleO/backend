package com.spots.domain.ai.service;


import static com.spots.global.exception.Code.JSON_CONVERSION_ERROR;
import static com.spots.global.exception.Code.LLM_INTERRUPT_ERROR;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spots.domain.ai.dto.request.RecommendLLMRequest;
import com.spots.domain.ai.dto.response.WeeklyRecommendResponse;
import com.spots.global.exception.CustomException;
import java.util.concurrent.Semaphore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RecommendLLMService {

  private final ChatClient chatClient;
  private final ObjectMapper objectMapper;
  public static final String REPLACE_CHARACTER = "user_data";
  private final int MAX_CONCURRENT_LLM_CALLS = 5;
  private final Semaphore llmSemaphore = new Semaphore(MAX_CONCURRENT_LLM_CALLS);

  @Value("classpath:prompt/routineV4.prompt")
  private Resource systemPromptResource;

  @Value("classpath:prompt/user-message.prompt")
  private Resource userPromptResource;

  public WeeklyRecommendResponse createWeeklyPlan(RecommendLLMRequest request) {

    try {
      llmSemaphore.acquire();
      String userJson = toJson(request);

      return chatClient
          .prompt()
          .system(s -> s.text(systemPromptResource))
          .user(u -> u.text(userPromptResource).param(REPLACE_CHARACTER, userJson))
          .call()
          .entity(WeeklyRecommendResponse.class);

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new CustomException(LLM_INTERRUPT_ERROR);
    } catch (Exception e) {
      log.error("LLM 호출 중 알 수 없는 에러 발생", e);
      throw new CustomException(LLM_INTERRUPT_ERROR);
    } finally {
      llmSemaphore.release();
    }
  }

  private String toJson(Object obj) {
    try {
      return objectMapper.writeValueAsString(obj);
    } catch (Exception e) {
      throw new CustomException(JSON_CONVERSION_ERROR);
    }
  }
}
