package dev.threadly.knowledge.service;

import dev.threadly.knowledge.entity.KbChunk;
import dev.threadly.knowledge.entity.KbDocument;
import dev.threadly.knowledge.entity.KbEmbedding;
import dev.threadly.knowledge.entity.KbIndexingJob;
import dev.threadly.knowledge.exception.DocumentIngestionException;
import dev.threadly.knowledge.repository.KbChunkRepository;
import dev.threadly.knowledge.repository.KbDocumentRepository;
import dev.threadly.knowledge.repository.KbEmbeddingRepository;
import dev.threadly.knowledge.repository.KbIndexingJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Service for document ingestion and indexing.
 * Orchestrates parsing, chunking, embedding, and storage of documents.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KbIngestionService {

  private final DocumentParserService documentParserService;
  private final ChunkingService chunkingService;
  private final EmbeddingService embeddingService;
  private final VectorDbService vectorDbService;
  private final KbDocumentService documentService;
  private final KbDocumentRepository documentRepository;
  private final KbChunkRepository chunkRepository;
  private final KbEmbeddingRepository embeddingRepository;
  private final KbIndexingJobRepository indexingJobRepository;

  /**
   * Start async ingestion of a document.
   *
   * @param documentId the document ID
   * @param inputStream the file input stream
   * @param contentType the MIME type
   * @param filename the filename
   */
  @Transactional
  public void startIngestion(String documentId, InputStream inputStream,
                             String contentType, String filename) {
    log.info("Starting ingestion for document: {}", documentId);

    try {
      // Get document details
      var document = documentRepository.findById(documentId)
          .orElseThrow(() -> new DocumentIngestionException("Document not found: " + documentId));

      // Create indexing job
      String jobId = UUID.randomUUID().toString();
      KbIndexingJob job = KbIndexingJob.builder()
          .id(jobId)
          .documentId(documentId)
          .botId(document.getBotId())
          .status(KbIndexingJob.JobStatus.PENDING)
          .progress(0)
          .processedChunks(0)
          .embeddedChunks(0)
          .createdAt(Instant.now())
          .build();

      indexingJobRepository.save(job);

      // Process document
      processDocument(documentId, document.getBotId(), inputStream, contentType, filename, jobId);

    } catch (Exception e) {
      log.error("Ingestion failed for document: {}", documentId, e);
      throw new DocumentIngestionException(documentId, "ingestion", e);
    }
  }

  /**
   * Process document (parse, chunk, embed, store).
   *
   * @param documentId the document ID
   * @param botId the bot ID
   * @param inputStream the file stream
   * @param contentType the MIME type
   * @param filename the filename
   * @param jobId the indexing job ID
   */
  @Transactional
  private void processDocument(String documentId, String botId, InputStream inputStream,
                               String contentType, String filename, String jobId) {
    KbIndexingJob job = indexingJobRepository.findById(jobId)
        .orElseThrow();

    try {
      // Update job status
      job.setStatus(KbIndexingJob.JobStatus.PROCESSING);
      job.setStartedAt(Instant.now());
      indexingJobRepository.save(job);

      // Step 1: Parse document
      log.info("Step 1: Parsing document");
      String content = documentParserService.parseDocument(inputStream, contentType, filename);
      updateJobProgress(jobId, 20);

      // Step 2: Chunk document
      log.info("Step 2: Chunking document");
      List<KbChunk> chunks = chunkingService.chunkDocument(documentId, botId, content);
      job.setTotalChunks(chunks.size());
      indexingJobRepository.save(job);
      updateJobProgress(jobId, 40);

      // Step 3: Save chunks
      log.info("Step 3: Saving {} chunks", chunks.size());
      chunkRepository.saveAll(chunks);
      updateJobProgress(jobId, 60);

      // Step 4: Generate embeddings
      log.info("Step 4: Generating embeddings");
      String modelName = embeddingService.getDefaultModel();
      int embeddedCount = 0;

      for (KbChunk chunk : chunks) {
        try {
          double[] embedding = embeddingService.generateEmbedding(chunk.getContent(), modelName);

          // Store embedding in vector DB
          var metadata = new HashMap<String, Object>();
          metadata.put("chunk_id", chunk.getId());
          metadata.put("document_id", documentId);
          metadata.put("chunk_number", chunk.getChunkNumber());
          vectorDbService.storeEmbedding(botId, chunk.getId(), embedding, metadata);

          // Save embedding entity
          KbEmbedding embeddingEntity = KbEmbedding.builder()
              .id(UUID.randomUUID().toString())
              .chunkId(chunk.getId())
              .embeddingModel(modelName)
              .embeddingJson(serializeEmbedding(embedding))
              .dimension(embedding.length)
              .build();
          embeddingRepository.save(embeddingEntity);

          // Mark chunk as embedded
          chunk.setIsEmbedded(true);
          chunkRepository.save(chunk);

          embeddedCount++;
          job.setEmbeddedChunks(embeddedCount);
          job.setProgress((int) (60 + (embeddedCount * 40 / chunks.size())));
          indexingJobRepository.save(job);

        } catch (Exception e) {
          log.error("Failed to embed chunk: {}", chunk.getId(), e);
          // Continue with next chunk
        }
      }

      // Step 5: Update document status
      log.info("Step 5: Updating document status");
      documentService.updateDocumentStatus(documentId, KbDocument.DocumentStatus.INDEXED);
      documentService.updateChunkCount(documentId, chunks.size());

      // Mark job as complete
      job.setStatus(KbIndexingJob.JobStatus.COMPLETE);
      job.setProgress(100);
      job.setProcessedChunks(chunks.size());
      job.setEmbeddedChunks(embeddedCount);
      job.setCompletedAt(Instant.now());
      indexingJobRepository.save(job);

      log.info("Document ingestion completed successfully: {}", documentId);

    } catch (Exception e) {
      log.error("Document ingestion failed: {}", documentId, e);

      // Mark job and document as failed
      job.setStatus(KbIndexingJob.JobStatus.FAILED);
      job.setErrorMessage(e.getMessage());
      job.setErrorStackTrace(getStackTrace(e));
      job.setCompletedAt(Instant.now());
      indexingJobRepository.save(job);

      documentService.updateDocumentStatus(documentId, KbDocument.DocumentStatus.FAILED);

      throw new DocumentIngestionException(documentId, "processing", e);
    }
  }

  /**
   * Update job progress.
   *
   * @param jobId the job ID
   * @param progress progress percentage (0-100)
   */
  @Transactional
  private void updateJobProgress(String jobId, int progress) {
    var job = indexingJobRepository.findById(jobId);
    if (job.isPresent()) {
      KbIndexingJob j = job.get();
      j.setProgress(progress);
      indexingJobRepository.save(j);
    }
  }

  /**
   * Serialize embedding as JSON string.
   *
   * @param embedding the embedding array
   * @return JSON string
   */
  private String serializeEmbedding(double[] embedding) {
    try {
      return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(embedding);
    } catch (Exception e) {
      log.warn("Failed to serialize embedding", e);
      return "[]";
    }
  }

  /**
   * Get stack trace as string.
   *
   * @param e the exception
   * @return stack trace
   */
  private String getStackTrace(Exception e) {
    StringBuilder sb = new StringBuilder();
    for (StackTraceElement element : e.getStackTrace()) {
      sb.append(element.toString()).append("\n");
    }
    return sb.toString();
  }

  /**
   * Get indexing job status.
   *
   * @param jobId the job ID
   * @return the job
   */
  @Transactional(readOnly = true)
  public KbIndexingJob getJobStatus(String jobId) {
    return indexingJobRepository.findById(jobId)
        .orElseThrow(() -> new DocumentIngestionException("Indexing job not found: " + jobId));
  }
}
