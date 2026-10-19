package com.sendclipper;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.http.HttpHeaders;

/**
 * What every failed call throws: an API refusal, or a request that never got an answer.
 *
 * <p>Branch on {@link #getName()}, SendClipper's machine-readable code; {@link #getMessage()} is a
 * sentence meant for a person.
 *
 * <pre>{@code
 * try {
 *   client.emails().send(params);
 * } catch (SendClipperException e) {
 *   if (e.getName().equals("domain_not_verified")) { ... }
 * }
 * }</pre>
 */
public class SendClipperException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  private final String name;
  private final int statusCode;
  private final transient JsonNode detail;
  private final transient HttpHeaders headers;

  /**
   * Makes an exception. The SDK makes these; you need this constructor only to fake one in a test.
   *
   * @param name the API's code
   * @param message a sentence meant for a person
   * @param statusCode the HTTP status, or 0 when no answer arrived
   * @param detail code-specific facts, or null
   * @param headers the response headers, or null when no answer arrived
   * @param cause the transport error behind a {@code network_error}, or null
   */
  public SendClipperException(
      String name,
      String message,
      int statusCode,
      JsonNode detail,
      HttpHeaders headers,
      Throwable cause) {
    super(message, cause);
    this.name = name;
    this.statusCode = statusCode;
    this.detail = detail;
    this.headers = headers;
  }

  /**
   * The API's code: {@code domain_not_verified}, {@code template_variables_missing}, {@code
   * validation_failed}, {@code rate_limited}, or {@code network_error} when no answer arrived.
   *
   * @return the code
   */
  public String getName() {
    return name;
  }

  /**
   * The HTTP status.
   *
   * @return the status, or 0 when no answer arrived (network, timeout, interrupted)
   */
  public int getStatusCode() {
    return statusCode;
  }

  /**
   * Code-specific facts, e.g. the missing template tags.
   *
   * @return the detail, or null
   */
  public JsonNode getDetail() {
    return detail;
  }

  /**
   * The response headers.
   *
   * @return the headers, or null when no answer arrived
   */
  public HttpHeaders getHeaders() {
    return headers;
  }

  @Override
  public String toString() {
    String status = statusCode == 0 ? "" : " (" + statusCode + ")";
    return "SendClipperException: " + name + status + ": " + getMessage();
  }
}
