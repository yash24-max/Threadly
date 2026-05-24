package dev.threadly.analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Analytics Service Application Entry Point.
 * Main Spring Boot application class for the Threadly analytics microservice.
 *
 * Features:
 * - Event tracking and aggregation from Kafka topics
 * - Real-time metrics computation and storage
 * - Daily rollup aggregations for efficient querying
 * - Custom dashboard support with caching
 * - Report generation (CSV, JSON, PDF)
 * - Multi-tenant analytics isolation via org_id
 *
 * Service runs on port 3007 and registers with Consul service discovery.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableAsync
public class AnalyticsServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(AnalyticsServiceApplication.class, args);
  }
}
