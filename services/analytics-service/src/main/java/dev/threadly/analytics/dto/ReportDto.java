package dev.threadly.analytics.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for analytics report configuration and metadata.
 * Represents a generated report with data and distribution settings.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportDto {

    @JsonProperty("id")
    private String id;

    @JsonProperty("org_id")
    private String orgId;

    @JsonProperty("report_type")
    private String reportType; // DAILY, WEEKLY, MONTHLY, CUSTOM

    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    @JsonProperty("start_date")
    private LocalDate startDate;

    @JsonProperty("end_date")
    private LocalDate endDate;

    @JsonProperty("bot_ids")
    private List<String> botIds;

    @JsonProperty("format")
    private String format; // PDF, CSV, JSON

    @JsonProperty("status")
    private String status; // PENDING, GENERATING, COMPLETED, FAILED

    @JsonProperty("file_url")
    private String fileUrl;

    @JsonProperty("file_size_bytes")
    private Long fileSizeBytes;

    @JsonProperty("generated_at")
    private Instant generatedAt;

    @JsonProperty("recipients")
    private List<String> recipients;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("updated_at")
    private Instant updatedAt;

}
