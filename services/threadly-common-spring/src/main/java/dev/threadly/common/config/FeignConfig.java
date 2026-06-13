package dev.threadly.common.config;

import feign.Client;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.okhttp.OkHttpClient;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Default Feign configuration for all microservice clients.
 *
 * Features:
 * - 10-second connection/read/write timeouts
 * - OkHttp client with connection pooling
 * - Spring message-converter-aware encoder/decoder
 */
@Slf4j
@Configuration
public class FeignConfig {

  private static final int TIMEOUT_SECONDS = 10;

  /** OkHttp client with timeouts and connection pooling. */
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

  /** Feign encoder backed by Spring's HttpMessageConverters. */
  @Bean
  @ConditionalOnMissingBean
  public Encoder feignEncoder(ObjectFactory<HttpMessageConverters> messageConverters) {
    return new SpringEncoder(messageConverters);
  }

  /** Feign decoder backed by Spring's HttpMessageConverters. */
  @Bean
  @ConditionalOnMissingBean
  public Decoder feignDecoder(ObjectFactory<HttpMessageConverters> messageConverters) {
    return new SpringDecoder(messageConverters);
  }
}
