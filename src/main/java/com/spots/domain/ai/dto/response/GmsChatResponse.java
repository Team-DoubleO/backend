package com.spots.domain.ai.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record GmsChatResponse(
    String id,
    String object,
    long created,
    String model,
    List<Choice> choices,
    Usage usage,
    @JsonProperty("service_tier")
    String serviceTier,
    @JsonProperty("system_fingerprint")
    String systemFingerprint
) {

  public String content() {
    if (choices == null || choices.isEmpty()) {
      return null;
    }
    return choices.get(0).message().content();
  }

  public record Choice(
      int index,
      Message message,
      @JsonProperty("finish_reason")
      String finishReason
  ) {

  }

  public record Message(
      String role,
      String content,
      Object refusal,
      List<Object> annotations
  ) {

  }

  public record Usage(
      @JsonProperty("prompt_tokens")
      int promptTokens,
      @JsonProperty("completion_tokens")
      int completionTokens,
      @JsonProperty("total_tokens")
      int totalTokens,
      @JsonProperty("prompt_tokens_details")
      PromptTokensDetails promptTokensDetails,
      @JsonProperty("completion_tokens_details")
      CompletionTokensDetails completionTokensDetails
  ) {

  }

  public record PromptTokensDetails(
      @JsonProperty("cached_tokens")
      int cachedTokens,
      @JsonProperty("audio_tokens")
      int audioTokens
  ) {

  }

  public record CompletionTokensDetails(
      @JsonProperty("reasoning_tokens")
      int reasoningTokens,
      @JsonProperty("audio_tokens")
      int audioTokens,
      @JsonProperty("accepted_prediction_tokens")
      int acceptedPredictionTokens,
      @JsonProperty("rejected_prediction_tokens")
      int rejectedPredictionTokens
  ) {

  }
}