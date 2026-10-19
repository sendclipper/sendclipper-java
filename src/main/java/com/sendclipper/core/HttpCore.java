package com.sendclipper.core;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.sendclipper.RequestOptions;
import com.sendclipper.SendClipperException;
import com.sendclipper.Version;
import com.sendclipper.model.DownloadedFile;
import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Sends requests, retries them and turns answers into models or {@link SendClipperException}s.
 * Internal: not part of the public API.
 */
public final class HttpCore {
  /** Waits between attempts; replaced in tests. */
  public interface Sleeper {
    /**
     * Sleeps.
     *
     * @param d how long
     * @throws InterruptedException when the thread is interrupted
     */
    void sleep(Duration d) throws InterruptedException;
  }

  private static final Duration MAX_RETRY_AFTER = Duration.ofSeconds(60);
  private static final int MAX_REDIRECTS = 10;

  private final String apiKey;
  private final String baseUrl;
  private final HttpClient http;
  private final Duration timeout;
  private final int maxRetries;
  private final Map<String, String> defaultHeaders;
  private final Sleeper sleeper;

  /**
   * Makes the engine.
   *
   * @param apiKey the API key, or null
   * @param baseUrl the API origin, without /v1
   * @param http the client to send through
   * @param timeout the per-attempt timeout
   * @param maxRetries how often a retryable failure is retried
   * @param defaultHeaders headers on every request
   * @param sleeper waits between attempts
   */
  public HttpCore(
      String apiKey,
      String baseUrl,
      HttpClient http,
      Duration timeout,
      int maxRetries,
      Map<String, String> defaultHeaders,
      Sleeper sleeper) {
    this.apiKey = apiKey;
    this.baseUrl = baseUrl;
    this.http = http;
    this.timeout = timeout;
    this.maxRetries = maxRetries;
    this.defaultHeaders = Collections.unmodifiableMap(new LinkedHashMap<>(defaultHeaders));
    this.sleeper = sleeper;
  }

  /**
   * The API origin requests go to, without /v1.
   *
   * @return the origin
   */
  public String baseUrl() {
    return baseUrl;
  }

  /**
   * Strips a trailing slash and /v1.
   *
   * @param url the origin as given
   * @return the normalised origin
   */
  public static String normaliseBaseUrl(String url) {
    String u = url.trim();
    while (u.endsWith("/")) {
      u = u.substring(0, u.length() - 1);
    }
    if (u.endsWith("/v1")) {
      u = u.substring(0, u.length() - 3);
    }
    return u;
  }

  // ------------------------------------------------------------------ calls

  /**
   * Sends and decodes a JSON answer.
   *
   * @param <T> the answer's type
   * @param req the request
   * @param opts per-call options, or null
   * @param type the answer's class
   * @return the decoded answer
   */
  public <T> T json(ApiRequest req, RequestOptions opts, Class<T> type) {
    return decode(execute(req, opts, true), Json.MAPPER.getTypeFactory().constructType(type));
  }

  /**
   * Sends and decodes a JSON answer of a generic type.
   *
   * @param <T> the answer's type
   * @param req the request
   * @param opts per-call options, or null
   * @param type the answer's type
   * @return the decoded answer
   */
  public <T> T json(ApiRequest req, RequestOptions opts, TypeReference<T> type) {
    return decode(execute(req, opts, true), Json.MAPPER.getTypeFactory().constructType(type));
  }

  /**
   * Sends and ignores the answer's body.
   *
   * @param req the request
   * @param opts per-call options, or null
   */
  public void discard(ApiRequest req, RequestOptions opts) {
    execute(req, opts, true);
  }

  /**
   * Sends and returns the body as text.
   *
   * @param req the request
   * @param opts per-call options, or null
   * @return the body
   */
  public String text(ApiRequest req, RequestOptions opts) {
    return new String(execute(req, opts, false).body, StandardCharsets.UTF_8);
  }

  /**
   * Sends and returns the body as a file.
   *
   * @param req the request
   * @param opts per-call options, or null
   * @return the file
   */
  public DownloadedFile bytes(ApiRequest req, RequestOptions opts) {
    Answer a = execute(req, opts, false);
    return new DownloadedFile(
        a.body,
        a.headers.firstValue("Content-Type").orElse(""),
        filenameOf(a.headers.firstValue("Content-Disposition").orElse("")));
  }

  @SuppressWarnings("unchecked")
  private static <T> T decode(Answer a, JavaType type) {
    if (isBlank(a.body)) {
      return null;
    }
    try {
      return (T) Json.MAPPER.readValue(a.body, type);
    } catch (IOException e) {
      throw new SendClipperException(
          "invalid_response",
          "SendClipper's answer could not be decoded: " + e.getMessage(),
          a.status,
          null,
          a.headers,
          e);
    }
  }

  // ---------------------------------------------------------------- engine

  private static final class Answer {
    final int status;
    final HttpHeaders headers;
    final byte[] body;

    Answer(int status, HttpHeaders headers, byte[] body) {
      this.status = status;
      this.headers = headers;
      this.body = body;
    }
  }

  private Answer execute(ApiRequest req, RequestOptions opts, boolean wantsJson) {
    RequestOptions o = opts == null ? RequestOptions.none() : opts;
    String token = req.bearer != null ? req.bearer : apiKey;
    if (token == null || token.isEmpty()) {
      throw new SendClipperException(
          "api_key_missing",
          "No SendClipper API key. Pass one to new SendClipper(\"sc_live_...\") or set"
              + " SENDCLIPPER_API_KEY.",
          401,
          null,
          null,
          null);
    }

    String url = baseUrl + "/v1" + req.path + req.queryString();
    Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    headers.put("Authorization", "Bearer " + token);
    headers.put("Accept", wantsJson ? "application/json" : "*/*");
    headers.put("User-Agent", "sendclipper-java/" + Version.VERSION);
    headers.putAll(defaultHeaders);
    headers.putAll(o.getHeaders());

    byte[] payload = null;
    if (req.form != null) {
      payload = req.form;
      headers.put("Content-Type", req.formType);
    } else if (req.body != null) {
      payload = Json.write(req.body);
      headers.put("Content-Type", "application/json");
    }

    boolean isRead = req.method.equals("GET") || req.method.equals("HEAD");
    // The API de-duplicates every JSON mutation on this header for 24 hours. A multipart body is
    // not fingerprinted server-side, so it gets no key and is never retried.
    if (!isRead && req.form == null) {
      String key = o.getIdempotencyKey();
      headers.put("Idempotency-Key", key != null && !key.isEmpty() ? key : "sc-java-" + UUID.randomUUID());
    } else if (o.getIdempotencyKey() != null && !o.getIdempotencyKey().isEmpty()) {
      headers.put("Idempotency-Key", o.getIdempotencyKey());
    }
    int retries = 0;
    if (isRead || headers.containsKey("Idempotency-Key")) {
      retries = o.getMaxRetries() != null ? o.getMaxRetries() : maxRetries;
    }
    Duration attemptTimeout = o.getTimeout() != null ? o.getTimeout() : timeout;

    for (int attempt = 0; ; attempt++) {
      Answer a;
      try {
        a = once(req.method, url, headers, payload, attemptTimeout);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw interrupted(e);
      } catch (IOException e) {
        if (attempt < retries) {
          pause(backoff(attempt));
          continue;
        }
        throw connectionError(e, req.method, req.path, attemptTimeout);
      }

      boolean ok = a.status >= 200 && a.status < 300;
      if (!ok && retryableStatus(a.status) && attempt < retries) {
        pause(waitFor(a.headers, attempt));
        continue;
      }
      if (!ok) {
        SendClipperException err = Errors.fromResponse(a.status, a.body, a.headers);
        // Our own earlier attempt is still running server-side (a timeout that was not a
        // failure). Its result is replayed once it finishes.
        if (err.getName().equals("idempotency_key_in_flight") && attempt < retries) {
          pause(waitFor(a.headers, attempt));
          continue;
        }
        throw err;
      }
      if (o.getOnResponseHeaders() != null) {
        o.getOnResponseHeaders().accept(a.headers);
      }
      return a;
    }
  }

  /** One attempt, following redirects by hand so the API key never reaches another host. */
  private Answer once(
      String method, String url, Map<String, String> headers, byte[] payload, Duration t)
      throws IOException, InterruptedException {
    URI uri = URI.create(url);
    String host = uri.getHost();
    String m = method;
    byte[] body = payload;
    Map<String, String> h = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    h.putAll(headers);
    for (int hop = 0; ; hop++) {
      HttpRequest.Builder b =
          HttpRequest.newBuilder(uri)
              .timeout(t)
              .method(
                  m,
                  body == null
                      ? HttpRequest.BodyPublishers.noBody()
                      : HttpRequest.BodyPublishers.ofByteArray(body));
      for (Map.Entry<String, String> e : h.entrySet()) {
        b.header(e.getKey(), e.getValue());
      }
      HttpResponse<byte[]> res = http.send(b.build(), HttpResponse.BodyHandlers.ofByteArray());
      int status = res.statusCode();
      String location = res.headers().firstValue("Location").orElse(null);
      boolean redirect =
          location != null && (status == 301 || status == 302 || status == 303 || status == 307 || status == 308);
      if (!redirect) {
        return new Answer(status, res.headers(), res.body() == null ? new byte[0] : res.body());
      }
      if (hop >= MAX_REDIRECTS) {
        throw new IOException("stopped after " + MAX_REDIRECTS + " redirects");
      }
      uri = uri.resolve(location);
      if (!host.equalsIgnoreCase(String.valueOf(uri.getHost()))) {
        h.remove("Authorization");
      }
      if (status == 303 || ((status == 301 || status == 302) && !m.equals("GET") && !m.equals("HEAD"))) {
        m = "GET";
        body = null;
        h.remove("Content-Type");
      }
    }
  }

  private void pause(Duration d) {
    try {
      if (!d.isZero() && !d.isNegative()) {
        sleeper.sleep(d);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw interrupted(e);
    }
  }

  private static SendClipperException interrupted(InterruptedException e) {
    return new SendClipperException(
        "network_error", "The request was interrupted.", 0, null, null, e);
  }

  private SendClipperException connectionError(
      IOException e, String method, String path, Duration t) {
    if (e instanceof HttpTimeoutException) {
      return new SendClipperException(
          "network_error",
          "SendClipper did not answer within " + humanDuration(t) + " (" + method + " /v1" + path + ").",
          0,
          null,
          null,
          e);
    }
    return new SendClipperException(
        "network_error",
        "Could not reach SendClipper at " + baseUrl + ": " + e,
        0,
        null,
        null,
        e);
  }

  // ---------------------------------------------------------------- helpers

  static boolean retryableStatus(int s) {
    return s == 408 || s == 429 || s == 500 || s == 502 || s == 503 || s == 504;
  }

  static Duration backoff(int attempt) {
    double base = Math.min(8, 0.5 * Math.pow(2, attempt));
    double secs = base / 2 + ThreadLocalRandom.current().nextDouble() * (base / 2);
    return Duration.ofMillis((long) (secs * 1000));
  }

  static Duration waitFor(HttpHeaders h, int attempt) {
    Duration d = retryAfter(h);
    return d != null ? d : backoff(attempt);
  }

  static Duration retryAfter(HttpHeaders h) {
    String raw = h.firstValue("Retry-After").orElse("").trim();
    if (raw.isEmpty()) {
      return null;
    }
    try {
      double secs = Double.parseDouble(raw);
      if (secs >= 0 && !Double.isInfinite(secs)) {
        return min(MAX_RETRY_AFTER, Duration.ofMillis((long) (secs * 1000)));
      }
      return null;
    } catch (NumberFormatException ignored) {
      // an HTTP date, below
    }
    try {
      ZonedDateTime at = ZonedDateTime.parse(raw, DateTimeFormatter.RFC_1123_DATE_TIME);
      Duration d = Duration.between(ZonedDateTime.now(at.getZone()), at);
      return min(MAX_RETRY_AFTER, d.isNegative() ? Duration.ZERO : d);
    } catch (DateTimeParseException e) {
      return null;
    }
  }

  private static Duration min(Duration a, Duration b) {
    return a.compareTo(b) <= 0 ? a : b;
  }

  static String filenameOf(String disposition) {
    if (disposition == null || disposition.isEmpty()) {
      return "";
    }
    String plain = "";
    for (String part : splitParams(disposition)) {
      int eq = part.indexOf('=');
      if (eq < 0) {
        continue;
      }
      String key = part.substring(0, eq).trim().toLowerCase(Locale.ROOT);
      String value = part.substring(eq + 1).trim();
      if (key.equals("filename*")) {
        // RFC 5987: charset'lang'percent-encoded
        int q = value.indexOf('\'');
        int q2 = q < 0 ? -1 : value.indexOf('\'', q + 1);
        if (q2 > 0) {
          try {
            return URLDecoder.decode(
                value.substring(q2 + 1).replace("+", "%2B"), StandardCharsets.UTF_8);
          } catch (IllegalArgumentException ignored) {
            // fall back to filename=
          }
        }
      } else if (key.equals("filename")) {
        plain = unquote(value);
      }
    }
    return plain;
  }

  private static List<String> splitParams(String s) {
    List<String> out = new ArrayList<>();
    StringBuilder cur = new StringBuilder();
    boolean quoted = false;
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '"' && (i == 0 || s.charAt(i - 1) != '\\')) {
        quoted = !quoted;
      }
      if (c == ';' && !quoted) {
        out.add(cur.toString());
        cur.setLength(0);
      } else {
        cur.append(c);
      }
    }
    out.add(cur.toString());
    return out;
  }

  private static String unquote(String v) {
    if (v.length() >= 2 && v.startsWith("\"") && v.endsWith("\"")) {
      return v.substring(1, v.length() - 1).replace("\\\"", "\"").replace("\\\\", "\\");
    }
    return v;
  }

  private static String humanDuration(Duration d) {
    long ms = d.toMillis();
    if (ms % 1000 == 0) {
      return (ms / 1000) + " s";
    }
    return ms + " ms";
  }

  private static boolean isBlank(byte[] body) {
    for (byte b : body) {
      if (b != ' ' && b != '\n' && b != '\r' && b != '\t') {
        return false;
      }
    }
    return true;
  }

}
