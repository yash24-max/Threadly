package dev.threadly.core.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

/** RFC 7807 Problem+JSON response. */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

  private String type;
  private String title;
  private int status;
  private String detail;
  private String traceId;
  private Object errors; // for validation errors

  public static ApiError of(int status, String title, String detail) {
    return ApiError.builder()
        .type("https://threadly.dev/errors/" + title.toLowerCase().replace(" ", "-"))
        .title(title)
        .status(status)
        .detail(detail)
        .build();
  }
}
