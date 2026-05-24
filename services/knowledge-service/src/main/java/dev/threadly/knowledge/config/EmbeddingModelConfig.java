package dev.threadly.knowledge.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for embedding models.
 * Supports multiple embedding providers (Voyage, OpenAI, local models).
 */
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "embedding")
@Data
public class EmbeddingModelConfig {

  /**
   * Default embedding model to use.
   */
  private String defaultModel = "voyage-ai-3";

  /**
   * Voyage AI API key (if using Voyage).
   */
  private String voyageApiKey;

  /**
   * OpenAI API key (if using OpenAI).
   */
  private String openaiApiKey;

  /**
   * Local model path (if using local model).
   */
  private String localModelPath;

  /**
   * Embedding model dimension (e.g., 1536 for OpenAI, 1024 for Voyage).
   */
  private Integer modelDimension = 1024;

  /**
   * Batch size for embedding generation.
   */
  private Integer batchSize = 32;

  /**
   * Request timeout in seconds.
   */
  private Integer timeoutSeconds = 60;

  /**
   * Maximum retries on failure.
   */
  private Integer maxRetries = 3;

  /**
   * Get model dimensions for different models.
   *
   * @param modelName the model name
   * @return dimension of the model
   */
  public Integer getModelDimension(String modelName) {
    return switch (modelName) {
      case "voyage-ai-3" -> 1024;
      case "voyage-ai-2" -> 1536;
      case "text-embedding-3-small" -> 1536;
      case "text-embedding-3-large" -> 3072;
      default -> modelDimension;
    };
  }

  /**
   * Get embedding provider type for a model.
   *
   * @param modelName the model name
   * @return provider type
   */
  public String getProviderType(String modelName) {
    if (modelName.contains("voyage")) {
      return "voyage";
    } else if (modelName.contains("embedding-3") || modelName.contains("text-embedding")) {
      return "openai";
    } else {
      return "local";
    }
  }
}
