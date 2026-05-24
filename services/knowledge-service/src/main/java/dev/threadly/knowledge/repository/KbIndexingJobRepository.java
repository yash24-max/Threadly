package dev.threadly.knowledge.repository;

import dev.threadly.knowledge.entity.KbIndexingJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository for KbIndexingJob entity.
 * Provides CRUD and custom query methods for tracking document ingestion jobs.
 */
@Repository
public interface KbIndexingJobRepository extends JpaRepository<KbIndexingJob, String> {

  /**
   * Find the most recent indexing job for a document.
   *
   * @param documentId the document ID
   * @return optional containing the most recent job
   */
  Optional<KbIndexingJob> findFirstByDocumentIdOrderByCreatedAtDesc(String documentId);

  /**
   * Find all jobs for a document.
   *
   * @param documentId the document ID
   * @return list of jobs
   */
  List<KbIndexingJob> findByDocumentId(String documentId);

  /**
   * Find jobs by status.
   *
   * @param status the job status
   * @return list of jobs
   */
  List<KbIndexingJob> findByStatus(KbIndexingJob.JobStatus status);

  /**
   * Find all pending jobs (for processing).
   *
   * @return list of pending jobs
   */
  List<KbIndexingJob> findByStatusOrderByCreatedAtAsc(KbIndexingJob.JobStatus status);

  /**
   * Find processing jobs for a bot.
   *
   * @param botId the bot ID
   * @param status the job status
   * @return list of jobs
   */
  List<KbIndexingJob> findByBotIdAndStatus(String botId, KbIndexingJob.JobStatus status);

  /**
   * Find jobs created after a certain date.
   *
   * @param createdAfter the cutoff date
   * @return list of jobs
   */
  List<KbIndexingJob> findByCreatedAtAfter(Instant createdAfter);

  /**
   * Count jobs by status for a bot.
   *
   * @param botId the bot ID
   * @param status the job status
   * @return count of jobs
   */
  long countByBotIdAndStatus(String botId, KbIndexingJob.JobStatus status);

  /**
   * Find the most recent completed job for a document.
   *
   * @param documentId the document ID
   * @return optional containing the job
   */
  @Query("SELECT j FROM KbIndexingJob j WHERE j.documentId = :documentId AND j.status = 'COMPLETE' ORDER BY j.completedAt DESC LIMIT 1")
  Optional<KbIndexingJob> findLastCompletedJob(@Param("documentId") String documentId);

  /**
   * Find failed jobs (for retry logic).
   *
   * @param status the failed status
   * @return list of failed jobs
   */
  @Query("SELECT j FROM KbIndexingJob j WHERE j.status = :status ORDER BY j.completedAt ASC")
  List<KbIndexingJob> findFailedJobs(@Param("status") KbIndexingJob.JobStatus status);

  /**
   * Delete old jobs (cleanup).
   *
   * @param olderThan the cutoff date
   */
  void deleteByCreatedAtBefore(Instant olderThan);
}
