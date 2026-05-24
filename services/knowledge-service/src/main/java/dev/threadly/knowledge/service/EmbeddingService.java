package dev.threadly.knowledge.service;

import dev.threadly.knowledge.config.EmbeddingModelConfig;
import dev.threadly.knowledge.exception.DocumentIngestionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service for generating embeddings for document chunks.
 * Supports multiple embedding providers (Voyage, OpenAI, local models).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {

  private final EmbeddingModelConfig embeddingConfig;
  private final RestTemplate restTemplate;

  /**
   * Generate embedding for a text chunk.
   *
   * @param text the text to embed
   * @param modelName the embedding model to use
   * @return array of floats representing the embedding
   */
  public double[] generateEmbedding(String text, String modelName) {
    log.debug("Generating embedding for text length: {} using model: {}", text.length(), modelName);

    try {
      String provider = embeddingConfig.getProviderType(modelName);
      return switch (provider) {
        case "voyage" -> generateVoyageEmbedding(text, modelName);
        case "openai" -> generateOpenAIEmbedding(text, modelName);
        case "local" -> generateLocalEmbedding(text, modelName);
        default -> throw new DocumentIngestionException("Unknown embedding provider: " + provider);
      };
    } catch (Exception e) {
      throw new DocumentIngestionException("embedding", "Failed to generate embedding: " + modelName, e);
    }
  }

  /**
   * Generate embeddings for multiple chunks (batch).
   *
   * @param texts list of texts to embed
   * @param modelName the embedding model
   * @return list of embedding arrays
   */
  public List<double[]> generateEmbeddingsBatch(List<String> texts, String modelName) {
    log.debug("Generating batch embeddings for {} texts using model: {}", texts.size(), modelName);

    List<double[]> embeddings = new ArrayList<>();
    String provider = embeddingConfig.getProviderType(modelName);

    if ("voyage".equals(provider)) {
      embeddings = generateVoyageEmbeddingsBatch(texts, modelName);
    } else if ("openai".equals(provider)) {
      embeddings = generateOpenAIEmbeddingsBatch(texts, modelName);
    } else {
      for (String text : texts) {
        embeddings.add(generateLocalEmbedding(text, modelName));
      }
    }

    return embeddings;
  }

  /**
   * Generate embedding using Voyage AI.
   *
   * @param text the text
   * @param modelName the model name
   * @return the embedding
   */
  private double[] generateVoyageEmbedding(String text, String modelName) {
    log.debug("Generating Voyage embedding");

    Map<String, Object> request = Map.of(
        "input", text,
        "model", modelName
    );

    try {
      @SuppressWarnings("unchecked")
      Map<String, Object> response = restTemplate.postForObject(
          "https://api.voyageai.com/v1/embeddings",
          createVoyageRequest(text, modelName),
          Map.class
      );

      if (response != null && response.containsKey("data")) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
        if (!data.isEmpty()) {
          @SuppressWarnings("unchecked")
          List<Double> embedding = (List<Double>) data.get(0).get("embedding");
          return embedding.stream().mapToDouble(Double::doubleValue).toArray();
        }
      }
      throw new DocumentIngestionException("No embedding returned from Voyage API");
    } catch (Exception e) {
      throw new DocumentIngestionException("embedding", "Voyage API call failed", e);
    }
  }

  /**
   * Generate batch embeddings using Voyage AI.
   *
   * @param texts the texts
   * @param modelName the model name
   * @return list of embeddings
   */
  private List<double[]> generateVoyageEmbeddingsBatch(List<String> texts, String modelName) {
    log.debug("Generating Voyage batch embeddings");

    List<double[]> embeddings = new ArrayList<>();
    for (String text : texts) {
      embeddings.add(generateVoyageEmbedding(text, modelName));
    }
    return embeddings;
  }

  /**
   * Generate embedding using OpenAI.
   *
   * @param text the text
   * @param modelName the model name
   * @return the embedding
   */
  private double[] generateOpenAIEmbedding(String text, String modelName) {
    log.debug("Generating OpenAI embedding");

    try {
      @SuppressWarnings("unchecked")
      Map<String, Object> response = restTemplate.postForObject(
          "https://api.openai.com/v1/embeddings",
          createOpenAIRequest(text, modelName),
          Map.class
      );

      if (response != null && response.containsKey("data")) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
        if (!data.isEmpty()) {
          @SuppressWarnings("unchecked")
          List<Double> embedding = (List<Double>) data.get(0).get("embedding");
          return embedding.stream().mapToDouble(Double::doubleValue).toArray();
        }
      }
      throw new DocumentIngestionException("No embedding returned from OpenAI");
    } catch (Exception e) {
      throw new DocumentIngestionException("embedding", "OpenAI API call failed", e);
    }
  }

  /**
   * Generate batch embeddings using OpenAI.
   *
   * @param texts the texts
   * @param modelName the model name
   * @return list of embeddings
   */
  private List<double[]> generateOpenAIEmbeddingsBatch(List<String> texts, String modelName) {
    log.debug("Generating OpenAI batch embeddings");

    List<double[]> embeddings = new ArrayList<>();
    for (String text : texts) {
      embeddings.add(generateOpenAIEmbedding(text, modelName));
    }
    return embeddings;
  }

  /**
   * Generate embedding using a local model (placeholder).
   *
   * @param text the text
   * @param modelName the model name
   * @return the embedding
   */
  private double[] generateLocalEmbedding(String text, String modelName) {
    log.debug("Generating local embedding");

    int dimension = embeddingConfig.getModelDimension(modelName);
    double[] embedding = new double[dimension];

    // Simple hash-based embedding for demonstration
    // In production, use a proper library like ONNX Runtime or TensorFlow
    int hash = text.hashCode();
    for (int i = 0; i < dimension; i++) {
      embedding[i] = Math.sin((hash + i) * 0.1) * 0.5 + 0.5;
    }

    return embedding;
  }

  /**
   * Create Voyage API request.
   *
   * @param text the text
   * @param modelName the model
   * @return request map
   */
  private Map<String, Object> createVoyageRequest(String text, String modelName) {
    return Map.of(
        "input", text,
        "model", modelName
    );
  }

  /**
   * Create OpenAI API request.
   *
   * @param text the text
   * @param modelName the model
   * @return request map
   */
  private Map<String, Object> createOpenAIRequest(String text, String modelName) {
    return Map.of(
        "input", text,
        "model", modelName
    );
  }

  /**
   * Get the default embedding model.
   *
   * @return model name
   */
  public String getDefaultModel() {
    return embeddingConfig.getDefaultModel();
  }

  /**
   * Get dimension for a model.
   *
   * @param modelName the model name
   * @return dimension
   */
  public Integer getModelDimension(String modelName) {
    return embeddingConfig.getModelDimension(modelName);
  }
}
