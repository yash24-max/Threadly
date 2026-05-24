package dev.threadly.conversation.controller;

import dev.threadly.conversation.dto.CaptureLeadRequest;
import dev.threadly.conversation.dto.LeadDto;
import dev.threadly.conversation.entity.Lead;
import dev.threadly.conversation.service.LeadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for lead management endpoints.
 * Provides APIs for capturing, retrieving, and managing leads from conversations.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class LeadController {
    private final LeadService leadService;

    /**
     * Capture a lead from a conversation.
     * POST /api/v1/conversations/{conversationId}/lead/capture
     *
     * @param conversationId the conversation ID
     * @param visitorId the visitor ID
     * @param request the lead capture request
     * @return the captured lead
     */
    @PostMapping("/conversations/{conversationId}/lead/capture")
    public ResponseEntity<LeadDto> captureLead(
        @RequestHeader("X-Org-ID") String orgId,
        @PathVariable String conversationId,
        @RequestParam String visitorId,
        @Valid @RequestBody CaptureLeadRequest request) {

        log.info("Capturing lead from conversation: {}", conversationId);
        LeadDto lead = leadService.captureLead(conversationId, orgId, visitorId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(lead);
    }

    /**
     * Get lead for a conversation.
     * GET /api/v1/conversations/{conversationId}/lead
     *
     * @param conversationId the conversation ID
     * @return the lead if exists
     */
    @GetMapping("/conversations/{conversationId}/lead")
    public ResponseEntity<LeadDto> getLeadByConversation(
        @PathVariable String conversationId) {

        var lead = leadService.getLeadByConversation(conversationId);
        return lead.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get a lead by ID.
     * GET /api/v1/leads/{leadId}
     *
     * @param leadId the lead ID
     * @return the lead
     */
    @GetMapping("/leads/{leadId}")
    public ResponseEntity<LeadDto> getLead(
        @RequestHeader("X-Org-ID") String orgId,
        @PathVariable String leadId) {

        log.debug("Fetching lead: {}", leadId);
        LeadDto lead = leadService.getLead(leadId, orgId);
        return ResponseEntity.ok(lead);
    }

    /**
     * List all leads for an organization.
     * GET /api/v1/leads?page=0&pageSize=20
     *
     * @param page the page number
     * @param pageSize the page size
     * @return page of leads
     */
    @GetMapping("/leads")
    public ResponseEntity<Page<LeadDto>> listLeads(
        @RequestHeader("X-Org-ID") String orgId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int pageSize) {

        log.debug("Listing leads for org: {}", orgId);
        Page<LeadDto> leads = leadService.listLeads(orgId, page, pageSize);
        return ResponseEntity.ok(leads);
    }

    /**
     * List leads by status.
     * GET /api/v1/leads/by-status?status=NEW
     *
     * @param status the lead status
     * @param page the page number
     * @param pageSize the page size
     * @return page of leads
     */
    @GetMapping("/leads/by-status")
    public ResponseEntity<Page<LeadDto>> listLeadsByStatus(
        @RequestHeader("X-Org-ID") String orgId,
        @RequestParam String status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int pageSize) {

        Lead.LeadStatus leadStatus = Lead.LeadStatus.valueOf(status.toUpperCase());
        Page<LeadDto> leads = leadService.listLeadsByStatus(orgId, leadStatus, page, pageSize);
        return ResponseEntity.ok(leads);
    }

    /**
     * Search leads by email or name.
     * GET /api/v1/leads/search?q=text
     *
     * @param searchText the search text
     * @param page the page number
     * @param pageSize the page size
     * @return page of matching leads
     */
    @GetMapping("/leads/search")
    public ResponseEntity<Page<LeadDto>> searchLeads(
        @RequestHeader("X-Org-ID") String orgId,
        @RequestParam String searchText,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int pageSize) {

        log.debug("Searching leads in org: {} for text: {}", orgId, searchText);
        Page<LeadDto> leads = leadService.searchLeads(orgId, searchText, page, pageSize);
        return ResponseEntity.ok(leads);
    }

    /**
     * Update lead status.
     * PATCH /api/v1/leads/{leadId}/status
     *
     * @param leadId the lead ID
     * @param status the new status
     * @return the updated lead
     */
    @PatchMapping("/leads/{leadId}/status")
    public ResponseEntity<LeadDto> updateLeadStatus(
        @RequestHeader("X-Org-ID") String orgId,
        @PathVariable String leadId,
        @RequestParam String status) {

        log.debug("Updating lead: {} status to: {}", leadId, status);
        Lead.LeadStatus leadStatus = Lead.LeadStatus.valueOf(status.toUpperCase());
        LeadDto lead = leadService.updateLeadStatus(leadId, orgId, leadStatus);
        return ResponseEntity.ok(lead);
    }

    /**
     * Update lead quality score.
     * PATCH /api/v1/leads/{leadId}/quality-score
     *
     * @param leadId the lead ID
     * @param score the quality score
     * @return the updated lead
     */
    @PatchMapping("/leads/{leadId}/quality-score")
    public ResponseEntity<LeadDto> updateLeadQualityScore(
        @RequestHeader("X-Org-ID") String orgId,
        @PathVariable String leadId,
        @RequestParam Integer score) {

        log.debug("Updating lead: {} quality score to: {}", leadId, score);
        LeadDto lead = leadService.updateLeadQualityScore(leadId, orgId, score);
        return ResponseEntity.ok(lead);
    }

    /**
     * Get high-quality leads.
     * GET /api/v1/leads/high-quality?minScore=70
     *
     * @param minScore the minimum quality score
     * @param page the page number
     * @param pageSize the page size
     * @return page of high-quality leads
     */
    @GetMapping("/leads/high-quality")
    public ResponseEntity<Page<LeadDto>> getHighQualityLeads(
        @RequestHeader("X-Org-ID") String orgId,
        @RequestParam Integer minScore,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int pageSize) {

        Page<LeadDto> leads = leadService.getHighQualityLeads(orgId, minScore, page, pageSize);
        return ResponseEntity.ok(leads);
    }

    /**
     * Get lead statistics.
     * GET /api/v1/leads/stats
     *
     * @return lead statistics
     */
    @GetMapping("/leads/stats")
    public ResponseEntity<?> getLeadStats(
        @RequestHeader("X-Org-ID") String orgId) {

        long newCount = leadService.countNewLeads(orgId);
        long convertedCount = leadService.countConvertedLeads(orgId);

        return ResponseEntity.ok(new Object() {
            public final long newLeads = newCount;
            public final long convertedLeads = convertedCount;
            public final double conversionRate = newCount > 0 ? (double) convertedCount / newCount * 100 : 0;
        });
    }
}
