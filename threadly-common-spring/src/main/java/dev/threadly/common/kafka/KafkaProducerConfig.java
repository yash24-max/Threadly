package dev.threadly.common.kafka;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

/**
 * Kafka producer configuration.
 *
 * Serialization:
 * - Keys: StringSerializer (topic, org_id, etc.)
 * - Values: JsonSerializer (event payload)
 *
 * Properties:
 * - acks=all: Wait for all replicas to acknowledge
 * - retries=3: Retry failed sends 3 times
 * - compression=snappy: Enable compression
 */
@Slf4j
@Configuration
@EnableKafka
public class KafkaProducerConfig {

  @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
  private String bootstrapServers;

  @Value("${spring.kafka.producer.acks:all}")
  private String acks;

  @Value("${spring.kafka.producer.retries:3}")
  private int retries;

  @Value("${spring.kafka.producer.compression-type:snappy}")
  private String compressionType;

  /**
   * Configure Kafka producer factory.
   */
  @Bean
  public ProducerFactory<String, Object> producerFactory() {
    Map<String, Object> configProps = new HashMap<>();
    configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    configProps.put(ProducerConfig.ACKS_CONFIG, acks);
    configProps.put(ProducerConfig.RETRIES_CONFIG, retries);
    configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, compressionType);
    configProps.put(ProducerConfig.LINGER_MS_CONFIG, 10); // Batch messages for 10ms
    configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

    log.info("Initializing Kafka Producer: bootstrapServers={}, acks={}, retries={}", bootstrapServers, acks, retries);

    return new DefaultKafkaProducerFactory<>(configProps);
  }

  /**
   * Kafka template for sending events.
   */
  @Bean
  public KafkaTemplate<String, Object> kafkaTemplate() {
    return new KafkaTemplate<>(producerFactory());
  }
}
