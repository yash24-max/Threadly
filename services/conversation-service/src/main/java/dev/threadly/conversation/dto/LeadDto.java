package dev.threadly.conversation.dto;

import dev.threadly.conversation.entity.Lead;
import lombok.*;

import java.time.Instant;

/**
 * Data Transfer Object for Lead entity.
 * Used for API responses and data serialization.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadDto {
    private String id;
    private String orgId;
    private String conversationId;
    private String visitorId;
    private String email;
    private String phone;
    private String name;
    private String company;
    private String customFieldsJson;
    private String status;
    private Integer qualityScore;
    private Instant capturedAt;
    private Instant updatedAt;

    /**
     * Convert a Lead entity to a DTO.
     *
     * @param lead the lead entity
     * @return the lead DTO
     */
    public static LeadDto fromEntity(Lead lead) {
        if (lead == null) {
            return null;
        }
        return LeadDto.builder()
            .id(lead.getId())
            .orgId(lead.getOrgId())
            .conversationId(lead.getConversationId())
            .visitorId(lead.getVisitorId())
            .email(lead.getEmail())
            .phone(lead.getPhone())
            .name(lead.getName())
            .company(lead.getCompany())
            .customFieldsJson(lead.getCustomFieldsJson())
            .status(lead.getStatus().toString())
            .qualityScore(lead.getQualityScore())
            .capturedAt(lead.getCapturedAt())
            .updatedAt(lead.getUpdatedAt())
            .build();
    }
}
