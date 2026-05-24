package dev.threadly.analytics.controller;

import dev.threadly.analytics.dto.ReportDto;
import dev.threadly.analytics.service.ReportGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for report generation endpoints.
 * Provides endpoints for generating and retrieving analytics reports.
 */
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final ReportGenerationService reportGenerationService;

    /**
     * Generate a new analytics report.
     * POST /api/v1/reports/generate
     */
    @PostMapping("/generate")
    public ResponseEntity<?> generateReport(
        @RequestHeader(value = "X-Org-ID") String orgId,
        @RequestParam(value = "type") String reportType,
        @RequestParam(value = "start_date") String startDateStr,
        @RequestParam(value = "end_date") String endDateStr,
        @RequestParam(value = "bot_ids", required = false) List<String> botIds,
        @RequestParam(value = "format", defaultValue = "csv") String format
    ) {
        try {
            log.debug("Generating {} report for org: {}", reportType, orgId);

            LocalDate startDate = LocalDate.parse(startDateStr);
            LocalDate endDate = LocalDate.parse(endDateStr);

            if (startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest().body("start_date must be before end_date");
            }

            ReportDto report = reportGenerationService.generateReport(
                orgId,
                reportType,
                startDate,
                endDate,
                botIds,
                format
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(report);
        } catch (Exception e) {
            log.error("Error generating report for org: {}", orgId, e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Get a generated report by ID.
     * GET /api/v1/reports/{reportId}
     */
    @GetMapping("/{reportId}")
    public ResponseEntity<?> getReport(
        @PathVariable String reportId
    ) {
        try {
            log.debug("Fetching report: {}", reportId);

            // In a real implementation, this would fetch from a database
            // For now, returning a placeholder response
            ReportDto report = ReportDto.builder()
                .id(reportId)
                .status("COMPLETED")
                .build();

            return ResponseEntity.ok(report);
        } catch (Exception e) {
            log.error("Error fetching report: {}", reportId, e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Email a report to recipients.
     * POST /api/v1/reports/{reportId}/email
     */
    @PostMapping("/{reportId}/email")
    public ResponseEntity<?> emailReport(
        @PathVariable String reportId,
        @RequestBody EmailRequest emailRequest
    ) {
        try {
            log.debug("Emailing report: {} to recipients: {}", reportId, emailRequest.getRecipients());

            if (emailRequest.getRecipients() == null || emailRequest.getRecipients().isEmpty()) {
                return ResponseEntity.badRequest().body("recipients list is required");
            }

            // In a real implementation, this would send emails and update the report status
            return ResponseEntity.ok().body("Report queued for email delivery");
        } catch (Exception e) {
            log.error("Error emailing report: {}", reportId, e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Request DTO for email report endpoint.
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class EmailRequest {
        private List<String> recipients;
        private String subject;
        private String message;
    }

}
