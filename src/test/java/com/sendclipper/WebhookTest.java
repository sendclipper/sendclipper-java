package com.sendclipper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sendclipper.model.WebhookEvent;
import com.sendclipper.model.WebhookEventType;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class WebhookTest {
  static final String SECRET = "whsec_test_secret";
  static final long T = 1756800000L;
  static final String BODY =
      "{\"id\":\"evt_1\",\"event\":\"email.delivered\",\"createdAt\":\"2026-09-06T09:41:02.518Z\",\"data\":{\"emailId\":\"e1\",\"messageId\":\"m1\",\"to\":\"user@acme.io\",\"domain\":\"send.acme.io\",\"subject\":\"Your receipt\",\"bounceType\":\"Permanent\"}}";
  // Computed with `openssl dgst -sha256 -hmac` over the Node SDK's body (without bounceType),
  // so it is checked separately in signMatchesTheOpensslVector.
  static final String NODE_BODY =
      "{\"id\":\"evt_1\",\"event\":\"email.delivered\",\"createdAt\":\"2026-09-06T09:41:02.518Z\",\"data\":{\"emailId\":\"e1\",\"messageId\":\"m1\",\"to\":\"user@acme.io\",\"domain\":\"send.acme.io\",\"subject\":\"Your receipt\"}}";
  static final String NODE_SIG = "76921f5ba77ae7f38686e8f147da3903c0ff87100cbd47ee466b7d646d198192";

  static final String HEADER = "t=" + T + ",v1=" + Webhook.sign(SECRET, T, bytes(BODY));

  static byte[] bytes(String s) {
    return s.getBytes(StandardCharsets.UTF_8);
  }

  static Webhook.Verifier at(long seconds) {
    return Webhook.verifier(SECRET).clock(Clock.fixed(Instant.ofEpochSecond(seconds), ZoneOffset.UTC));
  }

  static Map<String, List<String>> headers(String name, String value) {
    return Collections.singletonMap(name, Collections.singletonList(value));
  }

  static void refused(String reason, Runnable r) {
    WebhookVerificationException e = assertThrows(WebhookVerificationException.class, r::run);
    assertEquals(reason, e.getReason(), e.getMessage());
  }

  @Test
  void signMatchesTheOpensslVector() {
    assertEquals(NODE_SIG, Webhook.sign(SECRET, T, bytes(NODE_BODY)));
    WebhookEvent ev = at(T).verifySignature(bytes(NODE_BODY), "t=" + T + ",v1=" + NODE_SIG);
    assertEquals("evt_1", ev.getId());
  }

  @Test
  void aGenuineDeliveryVerifiesAndReturnsTheParsedEvent() {
    WebhookEvent ev = at(T + 10).verify(bytes(BODY), headers("X-SendClipper-Signature-V2", HEADER));
    assertEquals("evt_1", ev.getId());
    assertEquals(WebhookEventType.EMAIL_DELIVERED, ev.getEvent());
    assertEquals("email.delivered", ev.getEventName());
    assertEquals("user@acme.io", ev.getData().getTo());
    assertEquals("Your receipt", ev.getData().getRaw().get("subject"));
    assertEquals("Permanent", ev.getData().getRaw().get("bounceType"));
    assertEquals(2026, ev.getCreatedAt().atZone(ZoneOffset.UTC).getYear());
  }

  @Test
  void theHeaderIsFoundWhateverItsCase() {
    at(T).verify(bytes(BODY), headers("x-sendclipper-signature-v2", HEADER));
    at(T).verifySignature(bytes(BODY), HEADER);
  }

  @Test
  void theStaticHelpersUseTheRealClock() {
    long now = System.currentTimeMillis() / 1000;
    String h = Webhook.signatureHeader(SECRET, bytes(BODY));
    assertEquals("evt_1", Webhook.verifySignature(BODY, h, SECRET).getId());
    assertEquals("evt_1", Webhook.verify(BODY, headers(Webhook.SIGNATURE_HEADER, h), SECRET).getId());
    assertTrue(Webhook.parseSignatureHeader(h).get().getTimestamp() >= now);
    refused("timestamp_out_of_tolerance", () -> Webhook.verifySignature(BODY, HEADER, SECRET));
  }

  @Test
  void duringARotationEitherSignatureIsAccepted() {
    String other = "a".repeat(64);
    String sig = Webhook.sign(SECRET, T, bytes(BODY));
    for (String h :
        Arrays.asList("t=" + T + ",v1=" + other + ",v1=" + sig, HEADER + ",v1=" + other, " t=" + T + " , v1=" + sig + " ")) {
      at(T).verifySignature(bytes(BODY), h);
    }
    // Two header lines (a proxy that splits them) are read as one list.
    at(T).verify(bytes(BODY), Collections.singletonMap(Webhook.SIGNATURE_HEADER, Arrays.asList("t=" + T + ",v1=" + other, "v1=" + sig)));
  }

  @Test
  void aChangedBodyAWrongSecretOrAWrongSignatureIsRefused() {
    refused("signature_mismatch", () -> at(T).verifySignature(bytes(BODY.replace("e1", "e2")), HEADER));
    refused(
        "signature_mismatch",
        () ->
            Webhook.verifier("whsec_other")
                .clock(Clock.fixed(Instant.ofEpochSecond(T), ZoneOffset.UTC))
                .verifySignature(bytes(BODY), HEADER));
    refused("signature_mismatch", () -> at(T).verifySignature(bytes(BODY), "t=" + T + ",v1=" + "0".repeat(64)));
  }

  @Test
  void reEncodedJsonFailsBecauseTheSignatureCoversTheRawBytes() {
    String reEncoded = BODY.replace("\":\"", "\": \"");
    refused("signature_mismatch", () -> at(T).verifySignature(bytes(reEncoded), HEADER));
  }

  @Test
  void aTimestampOutsideTheToleranceIsRefused() {
    refused("timestamp_out_of_tolerance", () -> at(T + 301).verifySignature(bytes(BODY), HEADER));
    refused("timestamp_out_of_tolerance", () -> at(T - 301).verifySignature(bytes(BODY), HEADER));
    at(T + 300).verifySignature(bytes(BODY), HEADER);
    at(T + 3600).tolerance(Duration.ofHours(2)).verifySignature(bytes(BODY), HEADER);
  }

  @Test
  void aMissingOrMalformedHeaderIsRefused() {
    refused("missing_header", () -> at(T).verify(bytes(BODY), Collections.emptyMap()));
    refused("missing_header", () -> at(T).verify(bytes(BODY), null));
    refused("missing_header", () -> at(T).verifySignature(bytes(BODY), ""));
    refused("missing_header", () -> Webhook.verifier("").verifySignature(bytes(BODY), HEADER));
    refused("malformed_header", () -> at(T).verifySignature(bytes(BODY), "v1=abc"));
    refused("malformed_header", () -> at(T).verifySignature(bytes(BODY), "t=" + T));
    refused("malformed_header", () -> at(T).verifySignature(bytes(BODY), "t=12x,v1=abc"));
    refused("malformed_header", () -> at(T).verifySignature(bytes(BODY), "t=99999999999999999999,v1=abc"));
  }

  @Test
  void aSignedBodyThatIsNotAnEventIsRefused() {
    byte[] junk = bytes("not json");
    refused("invalid_payload", () -> at(T).verifySignature(junk, "t=" + T + ",v1=" + Webhook.sign(SECRET, T, junk)));
  }

  @Test
  void signatureHeaderBuildsWhatVerifyAccepts() {
    String h = Webhook.signatureHeader(SECRET, bytes(BODY), T);
    assertEquals(HEADER, h);
    Webhook.Signature s = Webhook.parseSignatureHeader(h).get();
    assertEquals(T, s.getTimestamp());
    assertEquals(1, s.getSignatures().size());
    assertTrue(Webhook.parseSignatureHeader("v1=abc").isEmpty());
    assertTrue(Webhook.parseSignatureHeader(null).isEmpty());
  }

  @Test
  void theClientsWebhooksVerifyIsTheSameCheck() {
    String h = Webhook.signatureHeader(SECRET, bytes(BODY));
    WebhookEvent ev = new SendClipper("k").webhooks().verify(bytes(BODY), headers(Webhook.SIGNATURE_HEADER, h), SECRET);
    assertEquals("evt_1", ev.getId());
  }

  @Test
  void anEventTypeThisSdkDoesNotKnowKeepsItsName() {
    byte[] body = bytes("{\"id\":\"evt_2\",\"event\":\"email.delivery_delayed\",\"createdAt\":\"2026-09-06T09:41:02Z\",\"data\":{}}");
    WebhookEvent ev = at(T).verifySignature(body, Webhook.signatureHeader(SECRET, body, T));
    assertEquals(WebhookEventType.UNKNOWN, ev.getEvent());
    assertEquals("email.delivery_delayed", ev.getEventName());
    assertTrue(ev.getData().getRaw().isEmpty());
  }
}
