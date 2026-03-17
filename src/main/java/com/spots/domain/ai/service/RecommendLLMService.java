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
import org.springframework.util.StopWatch;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RecommendLLMService {

  private final ChatClient chatClient;
  private final ObjectMapper objectMapper;
  private final int MAX_CONCURRENT_LLM_CALLS = 5;
  private final Semaphore llmSemaphore = new Semaphore(MAX_CONCURRENT_LLM_CALLS);

  @Value("classpath:prompt/routineV4.prompt")
  private Resource systemPromptResource;

  public WeeklyRecommendResponse createWeeklyPlan(RecommendLLMRequest request) {
    StopWatch stopWatch = new StopWatch("LLM_Generation_Task");

    try {
      stopWatch.start("1. Semaphore Acquire");
      llmSemaphore.acquire();
      stopWatch.stop();

      stopWatch.start("2. Prompt & JSON Prep");
      String userJson = toJson(request);
      String userMessage = """
          아래는 사용자 정보와 후보 운동 프로그램 목록입니다.
          이를 기반으로 일주일 운동 루틴 포토카드를 만들 JSON을 생성해주세요.
          
          <user_data>
          %s
          </user_data>
          """.formatted(userJson);
      stopWatch.stop();

      stopWatch.start("3. LLM API Call (External)");
      WeeklyRecommendResponse response = chatClient
          .prompt()
          .system(s -> s.text(systemPromptResource))
          .user(userMessage)
          .call()
          .entity(WeeklyRecommendResponse.class);
      stopWatch.stop();

      log.info(stopWatch.prettyPrint());
      return response;

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
