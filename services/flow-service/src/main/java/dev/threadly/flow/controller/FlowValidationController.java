package dev.threadly.flow.controller;

import dev.threadly.flow.dto.FlowValidationDto;
import dev.threadly.flow.service.FlowValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for Flow Validation operations.
 * Handles flow validation and validation status retrieval.
 */
@RestController
@RequestMapping("/api/v1/flows/{flowId}/validation")
@RequiredArgsConstructor
@Slf4j
public class FlowValidationController {

  private final FlowValidationService validationService;

  /**
   * Validates a flow.
   *
   * POST /api/v1/flows/{flowId}/validation/validate
   * @param flowId the flow ID
   * @return the validation result
   */
  @PostMapping("/validate")
  public ResponseEntity<FlowValidationDto> validateFlow(
      @PathVariable String flowId) {
    log.info("Validating flow: {}", flowId);

    FlowValidationDto validation = validationService.validateFlow(flowId);
    return ResponseEntity.ok(validation);
  }

  /**
   * Gets the validation status for a flow.
   *
   * GET /api/v1/flows/{flowId}/validation/status
   * @param flowId the flow ID
   * @return the validation status
   */
  @GetMapping("/status")
  public ResponseEntity<FlowValidationDto> getValidationStatus(
      @PathVariable String flowId) {
    log.debug("Retrieving validation status for flow: {}", flowId);

    FlowValidationDto validation = validationService.getValidationStatus(flowId);
    return ResponseEntity.ok(validation);
  }

  /**
   * Gets the validation status (alternate endpoint).
   *
   * GET /api/v1/flows/{flowId}/validate
   * @param flowId the flow ID
   * @return the validation status
   */
  @GetMapping
  public ResponseEntity<FlowValidationDto> getValidation(
      @PathVariable String flowId) {
    log.debug("Retrieving validation for flow: {}", flowId);

    FlowValidationDto validation = validationService.getValidationStatus(flowId);
    return ResponseEntity.ok(validation);
  }
}
