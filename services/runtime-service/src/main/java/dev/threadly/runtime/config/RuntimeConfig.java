package dev.threadly.runtime.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * RuntimeConfig provides beans and configuration for runtime service
 */
@Configuration
public class RuntimeConfig {

  /**
   * RestTemplate bean for HTTP calls
   */
  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  /**
   * ObjectMapper bean for JSON processing
   */
  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }
}
