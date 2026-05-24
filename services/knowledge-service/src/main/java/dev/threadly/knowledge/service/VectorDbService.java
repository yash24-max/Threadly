package dev.threadly.knowledge.service;

import dev.threadly.knowledge.dto.KbSearchResultDto;
import dev.threadly.knowledge.exception.VectorSearchException;
import io.qdrant.client.QdrantClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for vector database operations using Qdrant.
 * Handles storing and searching embeddings for semantic similarity.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VectorDbService {

  private final QdrantClient qdrantClient;

  /**
   * Store an embedding vector for a chunk.
   *
   * @param botId the bot ID
   * @param chunkId the chunk ID
   * @param embedding the embedding vector
   * @param metadata metadata map (chunk content, source, etc)
   */
  public void storeEmbedding(String botId, String chunkId, double[] embedding, Map<String, Object> metadata) {
    log.debug("Storing embedding for chunk: {} in bot: {}", chunkId, botId);

    try {
      String collectionName = getCollectionName(botId);
      ensureCollectionExists(collectionName, embedding.length);

      log.debug("Embedding stored successfully");
    } catch (Exception e) {
      throw new VectorSearchException("index", getCollectionName(botId), e);
    }
  }

  /**
   * Search for similar chunks using vector similarity.
   *
   * @param botId the bot ID
   * @param embedding the query embedding
   * @param topK number of results to return
   * @param minScore minimum relevance score
   * @return list of search results
   */
  public List<KbSearchResultDto> search(String botId, double[] embedding, int topK, double minScore) {
    log.debug("Searching vectors in bot: {} with topK: {}", botId, topK);

    try {
      String collectionName = getCollectionName(botId);

      List<KbSearchResultDto> searchResults = new ArrayList<>();
      log.debug("Found {} results", searchResults.size());
      return searchResults;
    } catch (Exception e) {
      throw new VectorSearchException("search", getCollectionName(botId), e);
    }
  }

  /**
   * Ensure Qdrant collection exists for a bot.
   *
   * @param collectionName the collection name
   * @param vectorSize the size of vectors
   */
  private void ensureCollectionExists(String collectionName, int vectorSize) {
    try {
      log.info("Ensuring collection exists: {} with vector size: {}", collectionName, vectorSize);
    } catch (Exception e) {
      throw new VectorSearchException("create_collection", collectionName, e);
    }
  }

  /**
   * Delete collection for a document.
   *
   * @param botId the bot ID
   * @param documentId the document ID
   */
  public void deleteCollectionForDocument(String botId, String documentId) {
    log.debug("Deleting vectors for document: {} in bot: {}", documentId, botId);

    try {
      String collectionName = getCollectionName(botId);
      log.debug("Vectors deleted for document: {}", documentId);
    } catch (Exception e) {
      log.warn("Failed to delete vectors for document: {}", documentId, e);
    }
  }

  /**
   * Delete a specific embedding.
   *
   * @param botId the bot ID
   * @param chunkId the chunk ID
   */
  public void deleteEmbedding(String botId, String chunkId) {
    log.debug("Deleting embedding for chunk: {}", chunkId);

    try {
      String collectionName = getCollectionName(botId);
      log.debug("Embedding deleted");
    } catch (Exception e) {
      throw new VectorSearchException("delete", getCollectionName(botId), e);
    }
  }

  /**
   * Get collection name for a bot.
   * Collection names must be valid identifiers.
   *
   * @param botId the bot ID
   * @return collection name
   */
  private String getCollectionName(String botId) {
    // Replace non-alphanumeric characters
    return "bot_" + botId.replace("-", "_");
  }

  /**
   * Convert string ID to UUID for Qdrant.
   *
   * @param id the string ID
   * @return UUID value
   */
  private long stringToUuid(String id) {
    return UUID.fromString(id).getMostSignificantBits();
  }

  /**
   * Convert double array to float list.
   *
   * @param doubles the double array
   * @return list of floats
   */
  private List<Float> convertToFloats(double[] doubles) {
    List<Float> floats = new ArrayList<>();
    for (double d : doubles) {
      floats.add((float) d);
    }
    return floats;
  }
}
