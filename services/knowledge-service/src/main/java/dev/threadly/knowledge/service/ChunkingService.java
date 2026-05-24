package dev.threadly.knowledge.service;

import dev.threadly.knowledge.entity.KbChunk;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Service for semantic chunking of document content.
 * Splits documents into chunks with token awareness and overlap.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChunkingService {

  @Value("${chunking.max-chunk-size:1000}")
  private Integer maxChunkSize;

  @Value("${chunking.min-chunk-size:100}")
  private Integer minChunkSize;

  @Value("${chunking.overlap-size:100}")
  private Integer overlapSize;

  @Value("${chunking.strategy:semantic}")
  private String chunkingStrategy;

  private static final Pattern SENTENCE_PATTERN = Pattern.compile("[.!?]+");
  private static final Pattern PARAGRAPH_PATTERN = Pattern.compile("\\n\\n+");
  private static final float TOKENS_PER_WORD = 1.3f;

  /**
   * Chunk document content into semantic chunks.
   *
   * @param documentId the document ID
   * @param botId the bot ID
   * @param content the document content
   * @return list of chunks
   */
  public List<KbChunk> chunkDocument(String documentId, String botId, String content) {
    log.info("Chunking document: {} using strategy: {}", documentId, chunkingStrategy);

    List<KbChunk> chunks = switch (chunkingStrategy.toLowerCase()) {
      case "semantic" -> semanticChunking(documentId, botId, content);
      case "fixed" -> fixedSizeChunking(documentId, botId, content);
      case "sentence" -> sentenceChunking(documentId, botId, content);
      default -> semanticChunking(documentId, botId, content);
    };

    log.info("Created {} chunks for document: {}", chunks.size(), documentId);
    return chunks;
  }

  /**
   * Semantic chunking based on sentences and paragraphs.
   *
   * @param documentId the document ID
   * @param botId the bot ID
   * @param content the content
   * @return list of chunks
   */
  private List<KbChunk> semanticChunking(String documentId, String botId, String content) {
    List<KbChunk> chunks = new ArrayList<>();
    String[] paragraphs = PARAGRAPH_PATTERN.split(content);

    StringBuilder currentChunk = new StringBuilder();
    int chunkNumber = 0;
    int currentTokens = 0;

    for (String paragraph : paragraphs) {
      if (paragraph.trim().isEmpty()) {
        continue;
      }

      String[] sentences = SENTENCE_PATTERN.split(paragraph);
      for (String sentence : sentences) {
        if (sentence.trim().isEmpty()) {
          continue;
        }

        int sentenceTokens = estimateTokens(sentence);

        // Check if adding this sentence exceeds max chunk size
        if (currentTokens + sentenceTokens > maxChunkSize && currentChunk.length() > 0) {
          chunks.add(createChunk(documentId, botId, chunkNumber++, currentChunk.toString(), currentTokens));
          currentChunk = new StringBuilder();
          currentTokens = 0;

          // Add overlap
          if (chunks.size() > 1) {
            KbChunk lastChunk = chunks.get(chunks.size() - 1);
            String overlapContent = lastChunk.getContent();
            if (overlapContent.length() > overlapSize) {
              currentChunk.append(overlapContent.substring(overlapContent.length() - overlapSize));
              currentTokens = estimateTokens(currentChunk.toString());
            }
          }
        }

        if (currentChunk.length() > 0) {
          currentChunk.append(" ");
        }
        currentChunk.append(sentence.trim());
        currentTokens += sentenceTokens;
      }
    }

    // Add remaining chunk
    if (currentChunk.length() > minChunkSize) {
      chunks.add(createChunk(documentId, botId, chunkNumber, currentChunk.toString(), currentTokens));
    }

    return chunks;
  }

  /**
   * Fixed-size chunking with no semantic awareness.
   *
   * @param documentId the document ID
   * @param botId the bot ID
   * @param content the content
   * @return list of chunks
   */
  private List<KbChunk> fixedSizeChunking(String documentId, String botId, String content) {
    List<KbChunk> chunks = new ArrayList<>();
    String[] words = content.split("\\s+");

    StringBuilder currentChunk = new StringBuilder();
    int chunkNumber = 0;
    int currentTokens = 0;

    for (String word : words) {
      int wordTokens = estimateTokens(word);

      if (currentTokens + wordTokens > maxChunkSize && currentChunk.length() > 0) {
        chunks.add(createChunk(documentId, botId, chunkNumber++, currentChunk.toString(), currentTokens));

        // Add overlap
        String overlapText = currentChunk.toString();
        if (overlapText.length() > overlapSize) {
          currentChunk = new StringBuilder(overlapText.substring(overlapText.length() - overlapSize));
        } else {
          currentChunk = new StringBuilder();
        }
        currentTokens = estimateTokens(currentChunk.toString());
      }

      if (currentChunk.length() > 0) {
        currentChunk.append(" ");
      }
      currentChunk.append(word);
      currentTokens += wordTokens;
    }

    if (currentChunk.length() > minChunkSize) {
      chunks.add(createChunk(documentId, botId, chunkNumber, currentChunk.toString(), currentTokens));
    }

    return chunks;
  }

  /**
   * Sentence-based chunking.
   *
   * @param documentId the document ID
   * @param botId the bot ID
   * @param content the content
   * @return list of chunks
   */
  private List<KbChunk> sentenceChunking(String documentId, String botId, String content) {
    List<KbChunk> chunks = new ArrayList<>();
    String[] sentences = SENTENCE_PATTERN.split(content);

    StringBuilder currentChunk = new StringBuilder();
    int chunkNumber = 0;
    int currentTokens = 0;

    for (String sentence : sentences) {
      if (sentence.trim().isEmpty()) {
        continue;
      }

      int sentenceTokens = estimateTokens(sentence);

      if (currentTokens + sentenceTokens > maxChunkSize && currentChunk.length() > 0) {
        chunks.add(createChunk(documentId, botId, chunkNumber++, currentChunk.toString(), currentTokens));
        currentChunk = new StringBuilder();
        currentTokens = 0;
      }

      if (currentChunk.length() > 0) {
        currentChunk.append(" ");
      }
      currentChunk.append(sentence.trim());
      currentTokens += sentenceTokens;
    }

    if (currentChunk.length() > minChunkSize) {
      chunks.add(createChunk(documentId, botId, chunkNumber, currentChunk.toString(), currentTokens));
    }

    return chunks;
  }

  /**
   * Create a chunk entity.
   *
   * @param documentId the document ID
   * @param botId the bot ID
   * @param chunkNumber the chunk number
   * @param content the chunk content
   * @param tokens the token count
   * @return the chunk entity
   */
  private KbChunk createChunk(String documentId, String botId, int chunkNumber,
                              String content, int tokens) {
    return KbChunk.builder()
        .id(UUID.randomUUID().toString())
        .documentId(documentId)
        .botId(botId)
        .chunkNumber(chunkNumber)
        .content(content)
        .tokens(tokens)
        .isEmbedded(false)
        .build();
  }

  /**
   * Estimate token count for text.
   * Uses simple heuristic: average 1.3 tokens per word.
   *
   * @param text the text
   * @return estimated token count
   */
  private int estimateTokens(String text) {
    if (text == null || text.isEmpty()) {
      return 0;
    }
    int wordCount = text.trim().split("\\s+").length;
    return (int) Math.ceil(wordCount * TOKENS_PER_WORD);
  }
}
