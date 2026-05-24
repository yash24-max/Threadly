package dev.threadly.knowledge.controller;

import dev.threadly.knowledge.dto.KbIndexingJobDto;
import dev.threadly.knowledge.service.KbIngestionService;
import dev.threadly.knowledge.repository.KbIndexingJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for knowledge base indexing job management.
 * Provides admin endpoints for tracking and managing ingestion jobs.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/kb/indexing-jobs")
@RequiredArgsConstructor
public class KbIndexingController {

  private final KbIngestionService ingestionService;
  private final KbIndexingJobRepository indexingJobRepository;

  /**
   * Get all indexing jobs.
   *
   * @param botId optional bot ID filter
   * @param status optional status filter
   * @return list of indexing job DTOs
   */
  @GetMapping
  public ResponseEntity<List<KbIndexingJobDto>> getIndexingJobs(
      @RequestParam(required = false) String botId,
      @RequestParam(required = false) String status) {

    log.debug("Fetching indexing jobs for bot: {}, status: {}", botId, status);

    List<KbIndexingJobDto> jobs;

    if (botId != null && status != null) {
      var jobStatus = Enum.valueOf(
          dev.threadly.knowledge.entity.KbIndexingJob.JobStatus.class,
          status.toUpperCase()
      );
      jobs = indexingJobRepository.findByBotIdAndStatus(botId, jobStatus).stream()
          .map(KbIndexingJobDto::fromEntity)
          .collect(Collectors.toList());
    } else if (botId != null) {
      jobs = indexingJobRepository.findAll().stream()
          .filter(j -> j.getBotId().equals(botId))
          .map(KbIndexingJobDto::fromEntity)
          .collect(Collectors.toList());
    } else if (status != null) {
      var jobStatus = Enum.valueOf(
          dev.threadly.knowledge.entity.KbIndexingJob.JobStatus.class,
          status.toUpperCase()
      );
      jobs = indexingJobRepository.findByStatus(jobStatus).stream()
          .map(KbIndexingJobDto::fromEntity)
          .collect(Collectors.toList());
    } else {
      jobs = indexingJobRepository.findAll().stream()
          .map(KbIndexingJobDto::fromEntity)
          .collect(Collectors.toList());
    }

    return ResponseEntity.ok(jobs);
  }

  /**
   * Get specific indexing job details.
   *
   * @param jobId the job ID
   * @return indexing job DTO
   */
  @GetMapping("/{jobId}")
  public ResponseEntity<KbIndexingJobDto> getIndexingJob(@PathVariable String jobId) {
    log.debug("Fetching indexing job: {}", jobId);

    var job = ingestionService.getJobStatus(jobId);
    return ResponseEntity.ok(KbIndexingJobDto.fromEntity(job));
  }

  /**
   * Reindex a document (create new indexing job).
   *
   * @param documentId the document ID
   * @param botId the bot ID
   * @return created job DTO
   */
  @PostMapping("/reindex/{documentId}")
  public ResponseEntity<KbIndexingJobDto> reindexDocument(
      @PathVariable String documentId,
      @RequestParam String botId) {

    log.info("Reindexing document: {} for bot: {}", documentId, botId);

    // In production, this would:
    // 1. Delete existing chunks and embeddings
    // 2. Fetch the original file
    // 3. Start fresh ingestion
    // For now, return a placeholder

    return ResponseEntity.ok(new KbIndexingJobDto());
  }

  /**
   * Get indexing job statistics.
   *
   * @param botId the bot ID
   * @return statistics map
   */
  @GetMapping("/stats/{botId}")
  public ResponseEntity<java.util.Map<String, Object>> getIndexingStats(@PathVariable String botId) {
    log.debug("Getting indexing statistics for bot: {}", botId);

    long pending = indexingJobRepository.countByBotIdAndStatus(
        botId,
        dev.threadly.knowledge.entity.KbIndexingJob.JobStatus.PENDING
    );
    long processing = indexingJobRepository.countByBotIdAndStatus(
        botId,
        dev.threadly.knowledge.entity.KbIndexingJob.JobStatus.PROCESSING
    );
    long complete = indexingJobRepository.countByBotIdAndStatus(
        botId,
        dev.threadly.knowledge.entity.KbIndexingJob.JobStatus.COMPLETE
    );
    long failed = indexingJobRepository.countByBotIdAndStatus(
        botId,
        dev.threadly.knowledge.entity.KbIndexingJob.JobStatus.FAILED
    );

    var stats = new java.util.HashMap<String, Object>();
    stats.put("pending", pending);
    stats.put("processing", processing);
    stats.put("complete", complete);
    stats.put("failed", failed);
    stats.put("total", pending + processing + complete + failed);

    return ResponseEntity.ok(stats);
  }
}
