package dev.threadly.conversation.dto;

import jakarta.validation.constraints.Email;
import lombok.*;

/**
 * Request DTO for capturing a lead from a conversation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaptureLeadRequest {
    /**
     * Lead email address.
     */
    @Email(message = "Invalid email format")
    private String email;

    /**
     * Lead phone number.
     */
    private String phone;

    /**
     * Lead full name.
     */
    private String name;

    /**
     * Lead company name.
     */
    private String company;

    /**
     * Custom fields as JSON.
     */
    private String customFieldsJson;

    /**
     * Lead quality score (0-100).
     */
    private Integer qualityScore;
}
