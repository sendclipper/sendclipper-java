package com.sendclipper;

import com.sendclipper.core.Json;
import com.sendclipper.model.WebhookEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Verifies webhook deliveries.
 *
 * <p>SendClipper signs {@code "{timestamp}.{rawBody}"} with HMAC-SHA256 and sends {@code
 * X-SendClipper-Signature-V2: t=<seconds>,v1=<hex>[,v1=<hex>]}. During a secret rotation there are
 * two {@code v1=} values; any match is accepted. A delivery signed more than five minutes away from
 * now is refused.
 *
 * <pre>{@code
 * // Spring: @RequestBody byte[] body, @RequestHeader HttpHeaders headers
 * WebhookEvent event = Webhook.verify(body, headers, System.getenv("SENDCLIPPER_WEBHOOK_SECRET"));
 *
 * // Servlet:
 * WebhookEvent event = Webhook.verifySignature(
 *     request.getInputStream().readAllBytes(),
 *     request.getHeader(Webhook.SIGNATURE_HEADER),
 *     secret);
 * }</pre>
 *
 * <p>Pass the body EXACTLY as received, never re-encoded JSON.
 */
public final class Webhook {
  /** The header SendClipper signs deliveries in. */
  public static final String SIGNATURE_HEADER = "X-SendClipper-Signature-V2";

  /** SendClipper's documented tolerance: five minutes. */
  public static final Duration DEFAULT_TOLERANCE = Duration.ofMinutes(5);

  private Webhook() {}

  /**
   * Verifies a delivery and returns its parsed event.
   *
   * @param payload the body exactly as received
   * @param headers the request's headers; the name is matched whatever its case
   * @param secret the endpoint's {@code whsec_} signing secret
   * @return the event
   * @throws WebhookVerificationException when the delivery is not genuine
   */
  public static WebhookEvent verify(
      byte[] payload, Map<String, ? extends Collection<String>> headers, String secret) {
    return verifier(secret).verify(payload, headers);
  }

  /**
   * Verifies a delivery whose body you hold as a string (decoded as UTF-8 from the wire).
   *
   * @param payload the body exactly as received
   * @param headers the request's headers
   * @param secret the endpoint's signing secret
   * @return the event
   * @throws WebhookVerificationException when the delivery is not genuine
   */
  public static WebhookEvent verify(
      String payload, Map<String, ? extends Collection<String>> headers, String secret) {
    return verifier(secret).verify(payload.getBytes(StandardCharsets.UTF_8), headers);
  }

  /**
   * Verifies a delivery when you hold only the {@value #SIGNATURE_HEADER} header's value.
   *
   * @param payload the body exactly as received
   * @param signature the header's value, or null
   * @param secret the endpoint's signing secret
   * @return the event
   * @throws WebhookVerificationException when the delivery is not genuine
   */
  public static WebhookEvent verifySignature(byte[] payload, String signature, String secret) {
    return verifier(secret).verifySignature(payload, signature);
  }

  /**
   * {@link #verifySignature(byte[], String, String)} for a body held as a string.
   *
   * @param payload the body exactly as received
   * @param signature the header's value, or null
   * @param secret the endpoint's signing secret
   * @return the event
   * @throws WebhookVerificationException when the delivery is not genuine
   */
  public static WebhookEvent verifySignature(String payload, String signature, String secret) {
    return verifier(secret).verifySignature(payload.getBytes(StandardCharsets.UTF_8), signature);
  }

  /**
   * A verifier whose tolerance or clock you can change.
   *
   * @param secret the endpoint's signing secret
   * @return the verifier
   */
  public static Verifier verifier(String secret) {
    return new Verifier(secret, DEFAULT_TOLERANCE, Clock.systemUTC());
  }

  /**
   * {@code hex(HMAC-SHA256(secret, "{timestamp}.{rawBody}"))}. Public for tests of your own
   * handler.
   *
   * @param secret the signing secret
   * @param timestamp seconds since the epoch
   * @param rawBody the body
   * @return the lowercase hex signature
   */
  public static String sign(String secret, long timestamp, byte[] rawBody) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      mac.update((timestamp + ".").getBytes(StandardCharsets.UTF_8));
      byte[] sum = mac.doFinal(rawBody);
      StringBuilder hex = new StringBuilder(sum.length * 2);
      for (byte b : sum) {
        hex.append(Character.forDigit((b >> 4) & 0xf, 16)).append(Character.forDigit(b & 0xf, 16));
      }
      return hex.toString();
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      throw new IllegalStateException("HmacSHA256 is unavailable", e);
    }
  }

  /**
   * A valid header value for a body signed now, e.g. to test your endpoint locally.
   *
   * @param secret the signing secret
   * @param rawBody the body
   * @return {@code t=<now>,v1=<hex>}
   */
  public static String signatureHeader(String secret, byte[] rawBody) {
    return signatureHeader(secret, rawBody, System.currentTimeMillis() / 1000);
  }

  /**
   * A valid header value for a body signed at a given time.
   *
   * @param secret the signing secret
   * @param rawBody the body
   * @param timestamp seconds since the epoch
   * @return {@code t=<timestamp>,v1=<hex>}
   */
  public static String signatureHeader(String secret, byte[] rawBody, long timestamp) {
    return "t=" + timestamp + ",v1=" + sign(secret, timestamp, rawBody);
  }

  /**
   * Reads {@code t=<seconds>,v1=<hex>[,v1=<hex>]}.
   *
   * @param header the header's value
   * @return the parts, or empty when there is no valid {@code t=}
   */
  public static Optional<Signature> parseSignatureHeader(String header) {
    if (header == null) {
      return Optional.empty();
    }
    Long timestamp = null;
    List<String> signatures = new ArrayList<>();
    for (String part : header.split(",")) {
      int eq = part.indexOf('=');
      if (eq < 0) {
        continue;
      }
      String key = part.substring(0, eq).trim();
      String value = part.substring(eq + 1).trim();
      if (key.equals("t") && !value.isEmpty() && value.chars().allMatch(c -> c >= '0' && c <= '9')) {
        try {
          timestamp = Long.parseLong(value);
        } catch (NumberFormatException ignored) {
          // too large: not a timestamp
        }
      } else if (key.equals("v1") && !value.isEmpty()) {
        signatures.add(value);
      }
    }
    return timestamp == null ? Optional.empty() : Optional.of(new Signature(timestamp, signatures));
  }

  /** The parts of a signature header. */
  public static final class Signature {
    private final long timestamp;
    private final List<String> signatures;

    Signature(long timestamp, List<String> signatures) {
      this.timestamp = timestamp;
      this.signatures = Collections.unmodifiableList(signatures);
    }

    /**
     * When the delivery was signed.
     *
     * @return seconds since the epoch
     */
    public long getTimestamp() {
      return timestamp;
    }

    /**
     * The {@code v1=} values: two during a secret rotation.
     *
     * @return the hex signatures
     */
    public List<String> getSignatures() {
      return signatures;
    }
  }

  /** Verifies deliveries with one secret, tolerance and clock. Immutable and thread-safe. */
  public static final class Verifier {
    private final String secret;
    private final Duration tolerance;
    private final Clock clock;

    private Verifier(String secret, Duration tolerance, Clock clock) {
      this.secret = secret;
      this.tolerance = tolerance;
      this.clock = clock;
    }

    /**
     * Replaces the five-minute tolerance.
     *
     * @param tolerance how far from now a signature may be
     * @return a new verifier
     */
    public Verifier tolerance(Duration tolerance) {
      return new Verifier(secret, Objects.requireNonNull(tolerance), clock);
    }

    /**
     * Sets the clock, for tests.
     *
     * @param clock the clock
     * @return a new verifier
     */
    public Verifier clock(Clock clock) {
      return new Verifier(secret, tolerance, Objects.requireNonNull(clock));
    }

    /**
     * Verifies a delivery.
     *
     * @param payload the body exactly as received
     * @param headers the request's headers
     * @return the event
     * @throws WebhookVerificationException when the delivery is not genuine
     */
    public WebhookEvent verify(byte[] payload, Map<String, ? extends Collection<String>> headers) {
      List<String> values = new ArrayList<>();
      if (headers != null) {
        for (Map.Entry<String, ? extends Collection<String>> e : headers.entrySet()) {
          if (e.getKey() != null && e.getKey().equalsIgnoreCase(SIGNATURE_HEADER) && e.getValue() != null) {
            values.addAll(e.getValue());
          }
        }
      }
      return verifySignature(payload, values.isEmpty() ? null : String.join(",", values));
    }

    /**
     * Verifies a delivery from the header's value.
     *
     * @param payload the body exactly as received
     * @param signature the {@value Webhook#SIGNATURE_HEADER} value, or null
     * @return the event
     * @throws WebhookVerificationException when the delivery is not genuine
     */
    public WebhookEvent verifySignature(byte[] payload, String signature) {
      if (secret == null || secret.isEmpty()) {
        throw new WebhookVerificationException("missing_header", "No webhook signing secret was given.");
      }
      if (signature == null || signature.isEmpty()) {
        throw new WebhookVerificationException(
            "missing_header", "The request carries no " + SIGNATURE_HEADER + " header.");
      }
      Optional<Signature> parsed = parseSignatureHeader(signature);
      if (!parsed.isPresent() || parsed.get().getSignatures().isEmpty()) {
        throw new WebhookVerificationException(
            "malformed_header", SIGNATURE_HEADER + " has no t= or no v1= value.");
      }
      long timestamp = parsed.get().getTimestamp();
      long drift = Math.abs(clock.millis() / 1000 - timestamp);
      if (drift > tolerance.getSeconds()) {
        throw new WebhookVerificationException(
            "timestamp_out_of_tolerance",
            "The delivery was signed "
                + drift
                + " s from now, outside the "
                + tolerance.getSeconds()
                + " s tolerance.");
      }
      byte[] body = payload == null ? new byte[0] : payload;
      byte[] expected = sign(secret, timestamp, body).getBytes(StandardCharsets.UTF_8);
      // ANY match, and every candidate is compared: a rotation sends two.
      boolean matched = false;
      for (String candidate : parsed.get().getSignatures()) {
        matched |= MessageDigest.isEqual(expected, candidate.getBytes(StandardCharsets.UTF_8));
      }
      if (!matched) {
        throw new WebhookVerificationException(
            "signature_mismatch", "The signature does not match this secret and body.");
      }
      try {
        WebhookEvent event = Json.MAPPER.readValue(body, WebhookEvent.class);
        if (event == null) {
          throw new WebhookVerificationException(
              "invalid_payload", "The signed body is not a webhook event.");
        }
        return event;
      } catch (IOException e) {
        throw new WebhookVerificationException(
            "invalid_payload", "The signed body is not a webhook event: " + e.getMessage());
      }
    }
  }
}
