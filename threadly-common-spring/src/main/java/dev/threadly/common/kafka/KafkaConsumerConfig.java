package dev.threadly.common.kafka;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;

/**
 * Kafka consumer configuration.
 *
 * Deserialization:
 * - Keys: StringDeserializer
 * - Values: JsonDeserializer (auto-infer type from payload)
 *
 * Properties:
 * - auto.offset.reset=earliest: Read from start if no offset
 * - enable.auto.commit=false: Manual commit for better error handling
 * - max.poll.records=100: Batch size per poll
 */
@Slf4j
@Configuration
@EnableKafka
public class KafkaConsumerConfig {

  @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
  private String bootstrapServers;

  @Value("${spring.kafka.consumer.group-id:threadly-service}")
  private String groupId;

  @Value("${spring.kafka.consumer.auto-offset-reset:earliest}")
  private String autoOffsetReset;

  /**
   * Configure Kafka consumer factory.
   */
  @Bean
  public ConsumerFactory<String, Object> consumerFactory() {
    Map<String, Object> configProps = new HashMap<>();
    configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
    configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
    configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // Manual commit
    configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
    configProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "java.util.HashMap");
    configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "dev.threadly.common");

    log.info("Initializing Kafka Consumer: bootstrapServers={}, groupId={}, autoOffsetReset={}",
        bootstrapServers, groupId, autoOffsetReset);

    return new DefaultKafkaConsumerFactory<>(configProps);
  }

  /**
   * Kafka listener container factory with manual commit mode.
   */
  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, Object> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory());
    factory.setConcurrency(3); // 3 concurrent listeners per topic
    factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL); // Manual commit
    factory.getContainerProperties().setIdleEventInterval(300000L); // 5-minute idle timeout
    return factory;
  }
}
