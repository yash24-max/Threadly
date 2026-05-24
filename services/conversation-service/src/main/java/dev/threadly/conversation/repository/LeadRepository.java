package dev.threadly.conversation.repository;

import dev.threadly.conversation.entity.Lead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Lead entity.
 * Provides database access and query methods for managing captured leads.
 */
@Repository
public interface LeadRepository extends JpaRepository<Lead, String> {

    /**
     * Find a lead by ID and organization ID (multi-tenancy).
     *
     * @param id the lead ID
     * @param orgId the organization ID
     * @return Optional containing the lead if found
     */
    @Query("SELECT l FROM Lead l WHERE l.id = :id AND l.orgId = :orgId AND l.deletedAt IS NULL")
    Optional<Lead> findByIdAndOrgId(@Param("id") String id, @Param("orgId") String orgId);

    /**
     * Find all leads for an organization with pagination.
     *
     * @param orgId the organization ID
     * @param pageable pagination information
     * @return page of leads
     */
    @Query("SELECT l FROM Lead l WHERE l.orgId = :orgId AND l.deletedAt IS NULL ORDER BY l.capturedAt DESC")
    Page<Lead> findByOrgId(@Param("orgId") String orgId, Pageable pageable);

    /**
     * Find leads by organization and status.
     *
     * @param orgId the organization ID
     * @param status the lead status
     * @param pageable pagination information
     * @return page of leads with the specified status
     */
    @Query("SELECT l FROM Lead l WHERE l.orgId = :orgId AND l.status = :status AND l.deletedAt IS NULL ORDER BY l.capturedAt DESC")
    Page<Lead> findByOrgIdAndStatus(
        @Param("orgId") String orgId,
        @Param("status") Lead.LeadStatus status,
        Pageable pageable
    );

    /**
     * Find a lead by email within an organization.
     *
     * @param orgId the organization ID
     * @param email the email address
     * @return Optional containing the lead if found
     */
    @Query("SELECT l FROM Lead l WHERE l.orgId = :orgId AND l.email = :email AND l.deletedAt IS NULL")
    Optional<Lead> findByOrgIdAndEmail(@Param("orgId") String orgId, @Param("email") String email);

    /**
     * Find a lead by phone within an organization.
     *
     * @param orgId the organization ID
     * @param phone the phone number
     * @return Optional containing the lead if found
     */
    @Query("SELECT l FROM Lead l WHERE l.orgId = :orgId AND l.phone = :phone AND l.deletedAt IS NULL")
    Optional<Lead> findByOrgIdAndPhone(@Param("orgId") String orgId, @Param("phone") String phone);

    /**
     * Find lead by conversation ID.
     *
     * @param conversationId the conversation ID
     * @return Optional containing the lead if found
     */
    @Query("SELECT l FROM Lead l WHERE l.conversationId = :conversationId AND l.deletedAt IS NULL")
    Optional<Lead> findByConversationId(@Param("conversationId") String conversationId);

    /**
     * Find leads by visitor ID within an organization.
     *
     * @param orgId the organization ID
     * @param visitorId the visitor ID
     * @param pageable pagination information
     * @return page of leads for the visitor
     */
    @Query("SELECT l FROM Lead l WHERE l.orgId = :orgId AND l.visitorId = :visitorId AND l.deletedAt IS NULL ORDER BY l.capturedAt DESC")
    Page<Lead> findByOrgIdAndVisitorId(
        @Param("orgId") String orgId,
        @Param("visitorId") String visitorId,
        Pageable pageable
    );

    /**
     * Find leads captured within a date range.
     *
     * @param orgId the organization ID
     * @param startDate the start date
     * @param endDate the end date
     * @param pageable pagination information
     * @return page of leads within the date range
     */
    @Query("SELECT l FROM Lead l WHERE l.orgId = :orgId AND l.capturedAt >= :startDate AND l.capturedAt <= :endDate AND l.deletedAt IS NULL ORDER BY l.capturedAt DESC")
    Page<Lead> findByOrgIdAndDateRange(
        @Param("orgId") String orgId,
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate,
        Pageable pageable
    );

    /**
     * Count new leads for an organization.
     *
     * @param orgId the organization ID
     * @return count of new leads
     */
    @Query("SELECT COUNT(l) FROM Lead l WHERE l.orgId = :orgId AND l.status = 'NEW' AND l.deletedAt IS NULL")
    long countNewLeads(@Param("orgId") String orgId);

    /**
     * Count converted leads for an organization.
     *
     * @param orgId the organization ID
     * @return count of converted leads
     */
    @Query("SELECT COUNT(l) FROM Lead l WHERE l.orgId = :orgId AND l.status = 'CONVERTED' AND l.deletedAt IS NULL")
    long countConvertedLeads(@Param("orgId") String orgId);

    /**
     * Find leads with minimum quality score.
     *
     * @param orgId the organization ID
     * @param minScore the minimum quality score
     * @param pageable pagination information
     * @return page of high-quality leads
     */
    @Query("SELECT l FROM Lead l WHERE l.orgId = :orgId AND l.qualityScore >= :minScore AND l.deletedAt IS NULL ORDER BY l.qualityScore DESC")
    Page<Lead> findByOrgIdAndMinQualityScore(
        @Param("orgId") String orgId,
        @Param("minScore") Integer minScore,
        Pageable pageable
    );

    /**
     * Search leads by email or name.
     *
     * @param orgId the organization ID
     * @param searchText the text to search for
     * @param pageable pagination information
     * @return page of matching leads
     */
    @Query("SELECT l FROM Lead l WHERE l.orgId = :orgId AND (LOWER(l.email) LIKE LOWER(CONCAT('%', :searchText, '%')) OR LOWER(l.name) LIKE LOWER(CONCAT('%', :searchText, '%'))) AND l.deletedAt IS NULL ORDER BY l.capturedAt DESC")
    Page<Lead> searchByEmailOrName(
        @Param("orgId") String orgId,
        @Param("searchText") String searchText,
        Pageable pageable
    );

    /**
     * Find all leads for an organization (for bulk operations).
     *
     * @param orgId the organization ID
     * @return list of all leads
     */
    @Query("SELECT l FROM Lead l WHERE l.orgId = :orgId AND l.deletedAt IS NULL ORDER BY l.capturedAt DESC")
    List<Lead> findAllByOrgId(@Param("orgId") String orgId);
}
