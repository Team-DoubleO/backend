package com.spots.domain.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spots.domain.ai.dto.request.RecommendLLMRequest;
import com.spots.domain.ai.dto.response.WeeklyRecommendResponse;
import com.spots.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.io.InputStream;
import java.util.concurrent.Semaphore;

import static com.spots.global.exception.Code.*;
import static java.nio.charset.StandardCharsets.UTF_8;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RecommendLLMService {

  private final ChatClient chatClient;
  private final ObjectMapper objectMapper;
  private final Semaphore llmSemaphore = new Semaphore(5);

  public WeeklyRecommendResponse createWeeklyPlan(RecommendLLMRequest request) {
    StopWatch stopWatch = new StopWatch("LLM_Generation_Task");

    try {
      stopWatch.start("1. Semaphore Acquire");
      llmSemaphore.acquire();
      stopWatch.stop();

      stopWatch.start("2. Prompt & JSON Prep");
      String systemMessage = loadPrompt("prompt/routineV4.prompt");
      String userMessage = """
          아래는 사용자 정보와 후보 운동 프로그램 목록입니다.
          이를 기반으로 일주일 운동 루틴 포토카드를 만들 JSON을 생성해주세요.
          
          <user_data>
          %s
          </user_data>
          """.formatted(toJson(request));
      stopWatch.stop();

      stopWatch.start("3. LLM API Call (External)");
      String llmResponse = chatClient
              .prompt()
              .system(systemMessage)
              .user(userMessage)
              .call()
              .content();
      stopWatch.stop();

      stopWatch.start("4. Response Parsing");
      WeeklyRecommendResponse response = objectMapper.readValue(sanitize(llmResponse), WeeklyRecommendResponse.class);
      stopWatch.stop();

      log.info(stopWatch.prettyPrint());

      return response;

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new CustomException(LLM_INTERRUPT_ERROR);
    } catch (JsonProcessingException e) {
      log.error("JSON 파싱 실패. Raw Response: {}", e.getMessage());
      throw new CustomException(INVALID_JSON_RESPONSE);
    } catch (Exception e) {
      log.error("LLM 호출 중 알 수 없는 에러 발생", e);
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
      return objectMapper.writeValueAsString(obj);
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