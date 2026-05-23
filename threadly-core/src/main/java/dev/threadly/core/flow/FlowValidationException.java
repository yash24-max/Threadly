package dev.threadly.core.flow;

import java.util.List;

public class FlowValidationException extends RuntimeException {

  private final List<String> errors;

  public FlowValidationException(List<String> errors) {
    super("Flow validation failed: " + errors);
    this.errors = errors;
  }

  public List<String> getErrors() {
    return errors;
  }
}
