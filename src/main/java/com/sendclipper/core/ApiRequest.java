package com.sendclipper.core;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/** One logical API call. Internal: not part of the public API. */
public final class ApiRequest {
  final String method;
  final String path;
  final Map<String, String> query = new LinkedHashMap<>();
  Object body;
  byte[] form;
  String formType;
  String bearer;

  private ApiRequest(String method, String path) {
    this.method = method;
    this.path = path;
  }

  /**
   * A GET of {@code /v1} + path.
   *
   * @param path the path, its segments already encoded with {@link #seg}
   * @return the request
   */
  public static ApiRequest get(String path) {
    return new ApiRequest("GET", path);
  }

  /**
   * A POST.
   *
   * @param path the path
   * @return the request
   */
  public static ApiRequest post(String path) {
    return new ApiRequest("POST", path);
  }

  /**
   * A PATCH.
   *
   * @param path the path
   * @return the request
   */
  public static ApiRequest patch(String path) {
    return new ApiRequest("PATCH", path);
  }

  /**
   * A PUT.
   *
   * @param path the path
   * @return the request
   */
  public static ApiRequest put(String path) {
    return new ApiRequest("PUT", path);
  }

  /**
   * A DELETE.
   *
   * @param path the path
   * @return the request
   */
  public static ApiRequest delete(String path) {
    return new ApiRequest("DELETE", path);
  }

  /**
   * Sets a JSON body.
   *
   * @param body the value to encode
   * @return this request
   */
  public ApiRequest body(Object body) {
    this.body = body;
    return this;
  }

  /**
   * Sets a multipart body.
   *
   * @param form the encoded form
   * @return this request
   */
  public ApiRequest form(Multipart form) {
    this.form = form.bytes();
    this.formType = form.contentType();
    return this;
  }

  /**
   * Replaces the API key with another bearer token (the sandbox JSON door).
   *
   * @param token the token
   * @return this request
   */
  public ApiRequest bearer(String token) {
    this.bearer = token;
    return this;
  }

  /**
   * Adds a query parameter; a null or empty value is left out.
   *
   * @param name the name
   * @param value the value
   * @return this request
   */
  public ApiRequest query(String name, String value) {
    if (value != null && !value.isEmpty()) {
      query.put(name, value);
    }
    return this;
  }

  /**
   * Adds a numeric query parameter; null is left out.
   *
   * @param name the name
   * @param value the value
   * @return this request
   */
  public ApiRequest query(String name, Integer value) {
    if (value != null) {
      query.put(name, value.toString());
    }
    return this;
  }

  /**
   * Adds a boolean query parameter ("true"/"false"); null is left out.
   *
   * @param name the name
   * @param value the value
   * @return this request
   */
  public ApiRequest query(String name, Boolean value) {
    if (value != null) {
      query.put(name, value.toString());
    }
    return this;
  }

  /**
   * Percent-encodes one path segment.
   *
   * @param segment the raw segment (an id)
   * @return the encoded segment
   */
  public static String seg(String segment) {
    if (segment == null || segment.isEmpty()) {
      throw new IllegalArgumentException("SendClipper: an id is required");
    }
    return URLEncoder.encode(segment, StandardCharsets.UTF_8).replace("+", "%20");
  }

  String queryString() {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, String> e : query.entrySet()) {
      sb.append(sb.length() == 0 ? '?' : '&')
          .append(URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8))
          .append('=')
          .append(URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8));
    }
    return sb.toString();
  }
}
