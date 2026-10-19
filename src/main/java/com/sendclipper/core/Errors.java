package com.sendclipper.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.sendclipper.SendClipperException;
import java.net.http.HttpHeaders;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Reads the API's refusals. Internal: not part of the public API. */
final class Errors {
  private Errors() {}

  /**
   * Reads the two shapes the API answers refusals in: the house envelope {@code { error: { code,
   * message, detail? } }} and Nest's default {@code { statusCode, message: string | string[], error
   * }} (request validation).
   */
  static SendClipperException fromResponse(int status, byte[] body, HttpHeaders headers) {
    JsonNode obj = Json.tree(body);
    if (obj != null && obj.isObject()) {
      SendClipperException e = fromEnvelope(obj.get("error"), status, headers);
      if (e != null) {
        return e;
      }
      JsonNode msg = obj.get("message");
      if (msg != null) {
        if (msg.isObject()) {
          e = fromEnvelope(msg.get("error"), status, headers);
          if (e != null) {
            return e;
          }
        }
        if (msg.isArray()) {
          List<String> parts = new ArrayList<>();
          msg.forEach(m -> parts.add(m.isTextual() ? m.asText() : m.toString()));
          return new SendClipperException(
              "validation_failed", String.join("; ", parts), status, msg, headers, null);
        }
        if (msg.isTextual()) {
          return new SendClipperException(
              codeForStatus(status), msg.asText(), status, null, headers, null);
        }
      }
    }
    String snippet = new String(body, StandardCharsets.UTF_8).trim();
    if (snippet.length() > 300) {
      snippet = snippet.substring(0, 300);
    }
    return new SendClipperException(
        codeForStatus(status),
        snippet.isEmpty() ? "HTTP " + status : snippet,
        status,
        null,
        headers,
        null);
  }

  private static SendClipperException fromEnvelope(JsonNode env, int status, HttpHeaders headers) {
    if (env == null || !env.isObject()) {
      return null;
    }
    JsonNode detail = env.get("detail");
    if (detail == null || detail.isNull()) {
      detail = env.get("details");
    }
    if (detail != null && detail.isNull()) {
      detail = null;
    }
    return new SendClipperException(
        nonEmpty(scalar(env.get("code")), codeForStatus(status)),
        nonEmpty(scalar(env.get("message")), "HTTP " + status),
        status,
        detail,
        headers,
        null);
  }

  private static String scalar(JsonNode n) {
    if (n == null || n.isNull()) {
      return "";
    }
    return n.isTextual() ? n.asText() : n.toString();
  }

  private static String nonEmpty(String s, String fallback) {
    return s.isEmpty() ? fallback : s;
  }

  static String codeForStatus(int status) {
    switch (status) {
      case 400:
        return "bad_request";
      case 401:
        return "unauthorized";
      case 403:
        return "forbidden";
      case 404:
        return "not_found";
      case 409:
        return "conflict";
      case 413:
        return "payload_too_large";
      case 429:
        return "rate_limited";
      default:
        return "http_" + status;
    }
  }
}
