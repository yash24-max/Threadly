package dev.threadly.conversation.service;

import dev.threadly.conversation.dto.CaptureLeadRequest;
import dev.threadly.conversation.dto.LeadDto;
import dev.threadly.conversation.entity.Lead;
import dev.threadly.conversation.exception.ConversationNotFoundException;
import dev.threadly.conversation.exception.LeadCaptureException;
import dev.threadly.conversation.repository.ConversationRepository;
import dev.threadly.conversation.repository.LeadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service class for lead management.
 * Handles lead capture, storage, and analytics for prospects from conversations.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class LeadService {
    private final LeadRepository leadRepository;
    private final ConversationRepository conversationRepository;

    /**
     * Capture a lead from a conversation.
     *
     * @param conversationId the conversation ID
     * @param orgId the organization ID
     * @param visitorId the visitor ID
     * @param request the lead capture request
     * @return the captured lead DTO
     * @throws ConversationNotFoundException if conversation not found
     * @throws LeadCaptureException if capture validation fails
     */
    public LeadDto captureLead(String conversationId, String orgId, String visitorId, CaptureLeadRequest request) {
        // Verify conversation exists
        conversationRepository.findByIdAndOrgId(conversationId, orgId)
            .orElseThrow(() -> new ConversationNotFoundException(conversationId, orgId));

        // Validate at least one contact field is provided
        if ((request.getEmail() == null || request.getEmail().isEmpty()) &&
            (request.getPhone() == null || request.getPhone().isEmpty())) {
            throw new LeadCaptureException(conversationId, "At least email or phone is required");
        }

        // Check for duplicate leads by email or phone
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            Optional<Lead> existingByEmail = leadRepository.findByOrgIdAndEmail(orgId, request.getEmail());
            if (existingByEmail.isPresent()) {
                log.warn("Duplicate lead detected by email: {}", request.getEmail());
                // Return existing lead instead of creating duplicate
                return LeadDto.fromEntity(existingByEmail.get());
            }
        }

        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            Optional<Lead> existingByPhone = leadRepository.findByOrgIdAndPhone(orgId, request.getPhone());
            if (existingByPhone.isPresent()) {
                log.warn("Duplicate lead detected by phone: {}", request.getPhone());
                return LeadDto.fromEntity(existingByPhone.get());
            }
        }

        Lead lead = Lead.builder()
            .id(UUID.randomUUID().toString())
            .orgId(orgId)
            .conversationId(conversationId)
            .visitorId(visitorId)
            .email(request.getEmail())
            .phone(request.getPhone())
            .name(request.getName())
            .company(request.getCompany())
            .customFieldsJson(request.getCustomFieldsJson())
            .status(Lead.LeadStatus.NEW)
            .qualityScore(request.getQualityScore())
            .build();

        Lead saved = leadRepository.save(lead);
        log.info("Lead captured from conversation: {} with ID: {}", conversationId, saved.getId());

        return LeadDto.fromEntity(saved);
    }

    /**
     * Get a lead by ID.
     *
     * @param leadId the lead ID
     * @param orgId the organization ID
     * @return the lead DTO
     */
    @Transactional(readOnly = true)
    public LeadDto getLead(String leadId, String orgId) {
        Lead lead = leadRepository.findByIdAndOrgId(leadId, orgId)
            .orElseThrow(() -> new LeadCaptureException(leadId, "Lead not found"));

        return LeadDto.fromEntity(lead);
    }

    /**
     * Get lead by conversation ID.
     *
     * @param conversationId the conversation ID
     * @return Optional containing the lead if exists
     */
    @Transactional(readOnly = true)
    public Optional<LeadDto> getLeadByConversation(String conversationId) {
        return leadRepository.findByConversationId(conversationId)
            .map(LeadDto::fromEntity);
    }

    /**
     * Update lead status.
     *
     * @param leadId the lead ID
     * @param orgId the organization ID
     * @param status the new status
     * @return the updated lead DTO
     */
    public LeadDto updateLeadStatus(String leadId, String orgId, Lead.LeadStatus status) {
        Lead lead = leadRepository.findByIdAndOrgId(leadId, orgId)
            .orElseThrow(() -> new LeadCaptureException(leadId, "Lead not found"));

        lead.setStatus(status);
        Lead updated = leadRepository.save(lead);

        log.debug("Lead status updated: {} -> {}", leadId, status);
        return LeadDto.fromEntity(updated);
    }

    /**
     * Update lead quality score.
     *
     * @param leadId the lead ID
     * @param orgId the organization ID
     * @param score the quality score (0-100)
     * @return the updated lead DTO
     */
    public LeadDto updateLeadQualityScore(String leadId, String orgId, Integer score) {
        if (score < 0 || score > 100) {
            throw new LeadCaptureException(leadId, "Quality score must be between 0 and 100");
        }

        Lead lead = leadRepository.findByIdAndOrgId(leadId, orgId)
            .orElseThrow(() -> new LeadCaptureException(leadId, "Lead not found"));

        lead.setQualityScore(score);
        Lead updated = leadRepository.save(lead);

        return LeadDto.fromEntity(updated);
    }

    /**
     * List all leads for an organization.
     *
     * @param orgId the organization ID
     * @param page the page number
     * @param pageSize the page size
     * @return page of lead DTOs
     */
    @Transactional(readOnly = true)
    public Page<LeadDto> listLeads(String orgId, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        return leadRepository.findByOrgId(orgId, pageable)
            .map(LeadDto::fromEntity);
    }

    /**
     * List leads by status.
     *
     * @param orgId the organization ID
     * @param status the lead status
     * @param page the page number
     * @param pageSize the page size
     * @return page of lead DTOs
     */
    @Transactional(readOnly = true)
    public Page<LeadDto> listLeadsByStatus(String orgId, Lead.LeadStatus status, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        return leadRepository.findByOrgIdAndStatus(orgId, status, pageable)
            .map(LeadDto::fromEntity);
    }

    /**
     * Search leads by email or name.
     *
     * @param orgId the organization ID
     * @param searchText the search text
     * @param page the page number
     * @param pageSize the page size
     * @return page of matching lead DTOs
     */
    @Transactional(readOnly = true)
    public Page<LeadDto> searchLeads(String orgId, String searchText, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        return leadRepository.searchByEmailOrName(orgId, searchText, pageable)
            .map(LeadDto::fromEntity);
    }

    /**
     * Get all leads for bulk export.
     *
     * @param orgId the organization ID
     * @return list of all lead DTOs
     */
    @Transactional(readOnly = true)
    public List<LeadDto> getAllLeadsForExport(String orgId) {
        return leadRepository.findAllByOrgId(orgId)
            .stream()
            .map(LeadDto::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Count new leads.
     *
     * @param orgId the organization ID
     * @return count of new leads
     */
    @Transactional(readOnly = true)
    public long countNewLeads(String orgId) {
        return leadRepository.countNewLeads(orgId);
    }

    /**
     * Count converted leads.
     *
     * @param orgId the organization ID
     * @return count of converted leads
     */
    @Transactional(readOnly = true)
    public long countConvertedLeads(String orgId) {
        return leadRepository.countConvertedLeads(orgId);
    }

    /**
     * Get high-quality leads.
     *
     * @param orgId the organization ID
     * @param minScore the minimum quality score
     * @param page the page number
     * @param pageSize the page size
     * @return page of high-quality lead DTOs
     */
    @Transactional(readOnly = true)
    public Page<LeadDto> getHighQualityLeads(String orgId, Integer minScore, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        return leadRepository.findByOrgIdAndMinQualityScore(orgId, minScore, pageable)
            .map(LeadDto::fromEntity);
    }
}
