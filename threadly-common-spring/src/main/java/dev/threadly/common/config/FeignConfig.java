package dev.threadly.common.config;

import feign.Client;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.okhttp.OkHttpClient;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

/**
 * Default Feign configuration for all microservice clients.
 *
 * Features:
 * - 10-second connection timeout
 * - 10-second read timeout
 * - OkHttp client with connection pooling
 * - Request/response logging
 */
@Slf4j
@Configuration
public class FeignConfig {

  private static final int TIMEOUT_SECONDS = 10;

  /**
   * Configure OkHttp client with timeouts and connection pooling.
   */
  @Bean
  @ConditionalOnMissingBean
  public Client feignClient() {
    okhttp3.OkHttpClient httpClient = new okhttp3.OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .connectionPool(new okhttp3.ConnectionPool(10, 5, TimeUnit.MINUTES))
        .build();

    return new OkHttpClient(httpClient);
  }

  /**
   * Feign encoder (request serialization).
   */
  @Bean
  @ConditionalOnMissingBean
  public Encoder feignEncoder() {
    return new SpringEncoder(new org.springframework.http.converter.FormHttpMessageConverter());
  }

  /**
   * Feign decoder (response deserialization).
   */
  @Bean
  @ConditionalOnMissingBean
  public Decoder feignDecoder() {
    return new SpringDecoder(
        () -> new org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter());
  }
}
