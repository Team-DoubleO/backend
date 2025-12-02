package com.spots.global.config;

import org.springdoc.core.customizers.QuerydslPredicateOperationCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfig {

  @Bean
  @ConditionalOnMissingBean
  public QuerydslPredicateOperationCustomizer querydslPredicateOperationCustomizer() {
    return null;
  }
}
