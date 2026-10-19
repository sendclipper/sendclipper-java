package com.sendclipper;

import static com.sendclipper.FakeApi.apiError;
import static com.sendclipper.FakeApi.drop;
import static com.sendclipper.FakeApi.list;
import static com.sendclipper.FakeApi.map;
import static com.sendclipper.FakeApi.ok;
import static com.sendclipper.FakeApi.status;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.sendclipper.FakeApi.Call;
import com.sendclipper.model.Attachment;
import com.sendclipper.model.BatchResult;
import com.sendclipper.model.Contact;
import com.sendclipper.model.CreateTemplateParams;
import com.sendclipper.model.DownloadedFile;
import com.sendclipper.model.EmailDetail;
import com.sendclipper.model.EmailRow;
import com.sendclipper.model.EmailStatus;
import com.sendclipper.model.ImportContactsParams;
import com.sendclipper.model.ListEmailsParams;
import com.sendclipper.model.ListSuppressionsParams;
import com.sendclipper.model.ListWebhookDeliveriesParams;
import com.sendclipper.model.SandboxMessage;
import com.sendclipper.model.SandboxParams;
import com.sendclipper.model.SendBatchResponse;
import com.sendclipper.model.SendClass;
import com.sendclipper.model.SendEmailParams;
import com.sendclipper.model.SentEmail;
import com.sendclipper.model.Suppression;
import com.sendclipper.model.TemplateAsset;
import com.sendclipper.model.UploadTemplateAssetParams;
import com.sendclipper.model.WaitForOptions;
import com.sendclipper.model.WebhookEventType;
import java.net.http.HttpHeaders;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ClientTest {
  static final Map<String, Object> SENT =
      map(
          "id", "0b6c0000-0000-4000-8000-000000000001",
          "messageId", "msg_9f2b7c10ad",
          "sesMessageId", null,
          "from", "hello@acme.io",
          "to", "ada@lumen.io",
          "domain", "acme.io",
          "subject", "Hi",
          "status", "queued",
          "createdAt", "2026-10-16T10:00:00.000Z",
          "templateId", null);

  static SendEmailParams msg() {
    return SendEmailParams.builder().from("a@acme.io").to("b@lumen.io").subject("s").html("h").build();
  }

  static JsonNode json(String s) {
    try {
      return FakeApi.MAPPER.readTree(s);
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

  static SendClipperException refused(Runnable r) {
    return assertThrows(SendClipperException.class, r::run);
  }

  // ------------------------------------------------------------------- sending

  @Test
  void sendPostsToV1EmailsWithTheKeyAndAnIdempotencyKey() {
    try (FakeApi f = new FakeApi(status(201, SENT).header("X-Request-Id", "r1"))) {
      AtomicReference<HttpHeaders> h = new AtomicReference<>();
      SentEmail email =
          f.client()
              .emails()
              .send(
                  SendEmailParams.builder()
                      .from("hello@acme.io")
                      .to("ada@lumen.io")
                      .subject("Hi")
                      .html("<p>Hi</p>")
                      .build(),
                  RequestOptions.builder().onResponseHeaders(h::set).build());
      assertEquals("0b6c0000-0000-4000-8000-000000000001", email.getId());
      assertEquals(EmailStatus.QUEUED, email.getStatus());
      assertEquals("2026-10-16T10:00:00Z", email.getCreatedAt().toString());
      assertNull(email.getTemplateId());
      assertEquals("r1", h.get().firstValue("X-Request-Id").orElse(null));

      Call c = f.call(0);
      assertEquals("POST", c.method);
      assertEquals("/v1/emails", c.path);
      assertEquals("Bearer sc_live_test", c.header("Authorization"));
      assertEquals("application/json", c.header("Content-Type"));
      assertEquals("application/json", c.header("Accept"));
      assertEquals("sendclipper-java/" + Version.VERSION, c.header("User-Agent"));
      assertTrue(
          c.header("Idempotency-Key")
              .matches("^sc-java-[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"),
          c.header("Idempotency-Key"));
      // HTML is not escaped: the body travels as written.
      assertEquals("{\"from\":\"hello@acme.io\",\"to\":\"ada@lumen.io\",\"subject\":\"Hi\",\"html\":\"<p>Hi</p>\"}", c.bodyText());
    }
  }

  @Test
  void aTemplateSendCarriesTemplateIdVariablesAndACallerKey() {
    try (FakeApi f = new FakeApi(status(201, SENT))) {
      f.client()
          .emails()
          .send(
              SendEmailParams.builder()
                  .from("hello@acme.io")
                  .to("ada@lumen.io")
                  .templateId("tpl_9f2b7c10ad42")
                  .variable("first_name", "Ada")
                  .trackOpens(false)
                  .sendClass(SendClass.MARKETING)
                  .build(),
              RequestOptions.builder().idempotencyKey("order-42-receipt").build());
      assertEquals("order-42-receipt", f.call(0).header("Idempotency-Key"));
      assertEquals(
          json(
              "{\"from\":\"hello@acme.io\",\"to\":\"ada@lumen.io\",\"templateId\":\"tpl_9f2b7c10ad42\","
                  + "\"variables\":{\"first_name\":\"Ada\"},\"sendClass\":\"marketing\",\"trackOpens\":false}"),
          f.call(0).json());
    }
  }

  static SendEmailParams replyToAndFile() {
    byte[] pdf = "%PDF-1.7\n".getBytes(StandardCharsets.US_ASCII);
    byte[] content = Arrays.copyOf(pdf, pdf.length + 2);
    content[pdf.length] = (byte) 0xff;
    content[pdf.length + 1] = (byte) 0xfe;
    return SendEmailParams.builder()
        .from("bookings@acme.io")
        .to("ada@lumen.io")
        .subject("Your booking")
        .html("<p>Attached.</p>")
        .replyTo("support@acme.io", "ops@acme.io")
        .attachment(Attachment.of("Q&A booking-42.pdf", content, "application/pdf"))
        .build();
  }

  static final String REPLY_TO_AND_FILE_BODY =
      "{\"from\":\"bookings@acme.io\",\"to\":\"ada@lumen.io\",\"subject\":\"Your booking\",\"html\":\"<p>Attached.</p>\","
          + "\"replyTo\":[\"support@acme.io\",\"ops@acme.io\"],"
          + "\"attachments\":[{\"filename\":\"Q&A booking-42.pdf\",\"content\":\"JVBERi0xLjcK//4=\",\"contentType\":\"application/pdf\"}]}";

  @Test
  void aSendCarriesReplyToAndBase64EncodedAttachments() {
    try (FakeApi f = new FakeApi(status(201, SENT))) {
      f.client().emails().send(replyToAndFile());
      assertEquals(json(REPLY_TO_AND_FILE_BODY), f.call(0).json());
      assertTrue(f.call(0).bodyText().contains("\"Q&A booking-42.pdf\""), "the filename is not escaped");
    }
  }

  @Test
  void theSandboxSendCarriesReplyToAndAttachmentsTooWithTheSandboxToken() {
    try (FakeApi f = new FakeApi(status(201, map("id", "sbx-msg")))) {
      assertEquals("sbx-msg", f.client().sandboxes().send("sc_sbx_abc", replyToAndFile()).getId());
      assertEquals("/v1/sandbox/emails", f.call(0).path);
      assertEquals("Bearer sc_sbx_abc", f.call(0).header("Authorization"));
      assertEquals(json(REPLY_TO_AND_FILE_BODY), f.call(0).json());
    }
  }

  @Test
  void anAttachmentWithNoContentTypeLeavesItOutAndCanBeReadFromDisk(@TempDir Path dir)
      throws Exception {
    Path file = dir.resolve("a.txt");
    Files.write(file, "hi?".getBytes(StandardCharsets.UTF_8));
    try (FakeApi f = new FakeApi(status(201, SENT))) {
      f.client()
          .emails()
          .send(
              SendEmailParams.builder()
                  .from("a@acme.io")
                  .to("b@lumen.io")
                  .subject("s")
                  .html("h")
                  .attachment(Attachment.fromPath(file))
                  .build());
      assertEquals(json("[{\"filename\":\"a.txt\",\"content\":\"aGk/\"}]"), f.call(0).json().get("attachments"));
    }
  }

  @Test
  void batchSendPostsEmailsAndReturnsPerItemResults() {
    Map<String, Object> r =
        map(
            "data",
            list(
                map("index", 0, "status", "accepted", "id", "e1", "messageId", "m1"),
                map("index", 1, "status", "failed", "error", map("code", "template_not_published", "message", "draft"))),
            "summary",
            map("submitted", 2, "accepted", 1, "failed", 1));
    try (FakeApi f = new FakeApi(ok(r), ok(r))) {
      List<SendEmailParams> items =
          Arrays.asList(
              msg(),
              SendEmailParams.builder().from("a@acme.io").to("y@lumen.io").templateId("tpl_draft").replyTo("support@acme.io").build());
      SendBatchResponse res = f.client().batch().send(items);
      assertEquals(1, res.getSummary().getAccepted());
      BatchResult failed = res.getData().get(1);
      assertFalse(failed.isAccepted());
      assertEquals("template_not_published", failed.getError().getCode());
      assertTrue(res.getData().get(0).isAccepted());
      f.client().emails().sendBatch(items);
      for (Call c : f.calls()) {
        assertEquals("/v1/emails/batch", c.path);
        assertNotNull(c.header("Idempotency-Key"));
        assertEquals(
            json(
                "{\"emails\":[{\"from\":\"a@acme.io\",\"to\":\"b@lumen.io\",\"subject\":\"s\",\"html\":\"h\"},"
                    + "{\"from\":\"a@acme.io\",\"to\":\"y@lumen.io\",\"templateId\":\"tpl_draft\",\"replyTo\":[\"support@acme.io\"]}]}"),
            c.json());
      }
    }
  }

  @Test
  void emptyParamsTravelAsJsonObjects() {
    try (FakeApi f = new FakeApi(ok(map()), ok(map()), ok(map()), ok(map()))) {
      SendClipper c = f.client();
      c.templates().create(null);
      c.templates().preview("t1", null);
      c.webhooks().dispatches().replayAll(null);
      c.templates().setTags("t1", null);
      assertEquals("{}", f.call(0).bodyText());
      assertEquals("{\"variables\":{}}", f.call(1).bodyText());
      assertEquals("{}", f.call(2).bodyText());
      assertEquals("{\"tagIds\":[]}", f.call(3).bodyText());
    }
  }

  @Test
  void aBuilderBuildsOnce() {
    SendEmailParams.Builder b = SendEmailParams.builder().from("a@acme.io");
    b.build();
    assertThrows(IllegalStateException.class, () -> b.to("x@y.io"));
  }

  // ------------------------------------------------------------------- retries

  @Test
  void a503IsRetriedWithTheSameIdempotencyKey() {
    try (FakeApi f = new FakeApi(apiError(503, "unavailable", "down"), status(201, SENT))) {
      f.client().emails().send(msg());
      assertEquals(2, f.calls().size());
      assertEquals(f.call(0).header("Idempotency-Key"), f.call(1).header("Idempotency-Key"));
      assertEquals(1, f.slept.size());
    }
  }

  @Test
  void aNetworkErrorIsRetriedThenAnswersNetworkErrorWithNoStatus() {
    // A POST: the JDK's HttpClient re-sends an idempotent GET once by itself on a dropped
    // connection, which would make the call count here depend on the JDK, not on the SDK.
    try (FakeApi f = new FakeApi(drop(), drop())) {
      SendClipperException e = refused(() -> f.builder().maxRetries(1).build().emails().send(msg()));
      assertEquals("network_error", e.getName());
      assertEquals(0, e.getStatusCode());
      assertNull(e.getHeaders());
      assertTrue(e.getMessage().startsWith("Could not reach SendClipper at " + f.url()), e.getMessage());
      assertNotNull(e.getCause(), "network_error wraps the transport error");
      assertEquals(2, f.calls().size());
      assertEquals(f.call(0).header("Idempotency-Key"), f.call(1).header("Idempotency-Key"));
    }
  }

  @Test
  void aTimeoutNamesTheLimitAndIsRetried() {
    try (FakeApi f = new FakeApi(ok(map()).delay(Duration.ofSeconds(1)), ok(map()).delay(Duration.ofSeconds(1)))) {
      SendClipperException e =
          refused(
              () ->
                  f.client()
                      .workspace()
                      .get(RequestOptions.builder().timeout(Duration.ofMillis(50)).maxRetries(1).build()));
      assertEquals("network_error", e.getName());
      assertTrue(e.getMessage().contains("within 50 ms (GET /v1/workspace)"), e.getMessage());
      assertInstanceOf(HttpTimeoutException.class, e.getCause());
      assertEquals(2, f.calls().size());
    }
  }

  @Test
  void anInterruptedThreadStopsAtOnceAndKeepsItsInterruptFlag() throws Exception {
    try (FakeApi f = new FakeApi(ok(list()).delay(Duration.ofSeconds(2)))) {
      SendClipper c = f.client();
      AtomicReference<Throwable> thrown = new AtomicReference<>();
      AtomicReference<Boolean> flag = new AtomicReference<>();
      Thread t =
          new Thread(
              () -> {
                try {
                  c.domains().list();
                } catch (Throwable e) {
                  thrown.set(e);
                  flag.set(Thread.currentThread().isInterrupted());
                }
              });
      long start = System.nanoTime();
      t.start();
      Thread.sleep(100);
      t.interrupt();
      t.join(5000);
      assertTrue(System.nanoTime() - start < 1_500_000_000L, "did not stop at once");
      SendClipperException e = assertInstanceOf(SendClipperException.class, thrown.get());
      assertEquals("network_error", e.getName());
      assertInstanceOf(InterruptedException.class, e.getCause());
      assertTrue(flag.get());
      assertEquals(1, f.calls().size());
    }
  }

  @Test
  void a429HonoursRetryAfterCappedAtAMinute() {
    try (FakeApi f =
        new FakeApi(
            apiError(429, "rate_limited", "slow").header("Retry-After", "3"),
            status(503, null).header("Retry-After", "86400"),
            ok(list()))) {
      f.client().domains().list();
      assertEquals(Arrays.asList(Duration.ofSeconds(3), Duration.ofMinutes(1)), f.slept);
    }
  }

  @Test
  void retryAfterAsAnHttpDateIsHonoured() {
    String in2s =
        java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME.format(
            java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC).plusSeconds(2));
    try (FakeApi f = new FakeApi(status(503, null).header("Retry-After", in2s), ok(list()))) {
      f.client().domains().list();
      Duration d = f.slept.get(0);
      assertTrue(!d.isNegative() && d.compareTo(Duration.ofSeconds(2)) <= 0, d.toString());
    }
  }

  @Test
  void backoffGrowsAndStaysBounded() {
    for (int a = 0; a < 10; a++) {
      double b = com.sendclipper.core.TestAccess.backoff(a).toMillis() / 1000.0;
      double base = Math.min(8.0, 0.5 * (1 << a));
      assertTrue(b >= base / 2 - 0.001 && b <= base, "attempt " + a + ": " + b);
    }
  }

  @Test
  void idempotencyKeyInFlightIsRetriedAndAnyOther409IsNot() {
    try (FakeApi f =
        new FakeApi(apiError(409, "idempotency_key_in_flight", "wait").header("Retry-After", "0"), status(201, SENT))) {
      f.client().emails().send(msg());
      assertEquals(2, f.calls().size());
    }
    try (FakeApi f = new FakeApi(apiError(409, "sandbox_name_taken", "taken"))) {
      SendClipperException e =
          refused(() -> f.client().sandboxes().create(SandboxParams.builder().name("x").build()));
      assertEquals("sandbox_name_taken", e.getName());
      assertEquals("taken", e.getMessage());
      assertEquals(409, e.getStatusCode());
      assertEquals(1, f.calls().size());
    }
  }

  @Test
  void zeroRetriesMeansOneAttempt() {
    try (FakeApi f = new FakeApi(apiError(503, "unavailable", "down"))) {
      SendClipperException e =
          refused(() -> f.client().domains().list(RequestOptions.builder().maxRetries(0).build()));
      assertEquals(503, e.getStatusCode());
      assertEquals(1, f.calls().size());
    }
  }

  // ------------------------------------------------------------------- errors

  @Test
  void a4xxIsNotRetriedAndTheEnvelopeBecomesATypedError() {
    try (FakeApi f =
        new FakeApi(
            status(
                400,
                map("error", map("code", "template_variables_missing", "message", "Missing first_name", "detail", map("missing", list("first_name"))))))) {
      SendClipperException e =
          refused(
              () ->
                  f.client()
                      .emails()
                      .send(SendEmailParams.builder().from("a@acme.io").to("b@lumen.io").templateId("tpl_x").build()));
      assertEquals("template_variables_missing", e.getName());
      assertEquals("Missing first_name", e.getMessage());
      assertEquals(400, e.getStatusCode());
      assertEquals(json("{\"missing\":[\"first_name\"]}"), e.getDetail());
      assertEquals("first_name", e.getDetail().get("missing").get(0).asText());
      assertNotNull(e.getHeaders());
      assertEquals(1, f.calls().size());
      assertEquals("SendClipperException: template_variables_missing (400): Missing first_name", e.toString());
    }
  }

  @Test
  void aValidationErrorInNestsDefaultShapeBecomesValidationFailed() {
    try (FakeApi f =
        new FakeApi(
            status(400, map("statusCode", 400, "message", list("to must be a valid email address", "subject should not be empty"), "error", "Bad Request")))) {
      SendClipperException e = refused(() -> f.client().emails().send(msg()));
      assertEquals("validation_failed", e.getName());
      assertEquals("to must be a valid email address; subject should not be empty", e.getMessage());
      assertTrue(e.getDetail().isArray());
    }
  }

  @Test
  void aNestedEnvelopeAndAStringMessageAreRead() {
    try (FakeApi f =
        new FakeApi(
            status(403, map("message", map("error", map("code", "api_key_domain_scoped", "message", "scoped")))),
            status(404, map("statusCode", 404, "message", "Cannot GET /v1/nope")))) {
      SendClipper c = f.client();
      SendClipperException e = refused(() -> c.workspace().get());
      assertEquals("api_key_domain_scoped", e.getName());
      e = refused(() -> c.workspace().get());
      assertEquals("not_found", e.getName());
      assertEquals("Cannot GET /v1/nope", e.getMessage());
    }
  }

  @Test
  void aNonJsonErrorBodyStillAnswersACode() {
    try (FakeApi f = new FakeApi(status(502, "<html>Bad gateway</html>"))) {
      SendClipperException e = refused(() -> f.builder().maxRetries(0).build().domains().list());
      assertEquals("http_502", e.getName());
      assertEquals("<html>Bad gateway</html>", e.getMessage());
    }
  }

  @Test
  void anUndecodableAnswerIsAnInvalidResponseError() {
    try (FakeApi f = new FakeApi(ok(map("createdAt", "not a time")))) {
      SendClipperException e = refused(() -> f.client().workspace().get());
      assertEquals("invalid_response", e.getName());
    }
  }

  @Test
  void anUnknownEnumValueAndUnknownFieldsDoNotBreakDecoding() {
    Map<String, Object> row = new java.util.LinkedHashMap<>(SENT);
    row.put("status", "deferred_by_the_future");
    row.put("brandNewField", map("x", 1));
    try (FakeApi f = new FakeApi(ok(row))) {
      EmailDetail d = f.client().emails().get("e1");
      assertEquals(EmailStatus.UNKNOWN, d.getStatus());
      assertTrue(d.toString().startsWith("EmailDetail{"), d.toString());
    }
  }

  @Test
  void noKeyAnywhereIsAClearApiKeyMissingAndNoRequestIsMade() {
    try (FakeApi f = new FakeApi()) {
      SendClipper c = SendClipper.builder().baseUrl(f.url()).env(n -> null).build();
      SendClipperException e = refused(() -> c.domains().list());
      assertEquals("api_key_missing", e.getName());
      assertEquals(401, e.getStatusCode());
      assertEquals(0, f.calls().size());
    }
  }

  // ------------------------------------------------------------------- requests

  @Test
  void getRequestsCarryNoIdempotencyKeyAndDropEmptyQueryValues() {
    try (FakeApi f = new FakeApi(ok(map("items", list(), "total", 0)))) {
      f.client()
          .emails()
          .list(ListEmailsParams.builder().status(EmailStatus.BOUNCED).search("").pageSize(50).build());
      assertNull(f.call(0).header("Idempotency-Key"));
      assertEquals(map("status", "bounced", "pageSize", "50"), f.call(0).query);
    }
  }

  @Test
  void aCallerKeyOnAReadIsSentButOnlyWhenGiven() {
    try (FakeApi f = new FakeApi(ok(list()))) {
      f.client().domains().list(RequestOptions.builder().idempotencyKey("read-1").build());
      assertEquals("read-1", f.call(0).header("Idempotency-Key"));
    }
  }

  @Test
  void booleansInTheQueryAreWrittenTheWayTheApiReadsThem() {
    try (FakeApi f = new FakeApi(ok(list()), ok(list()))) {
      f.client().mailboxes().list(true);
      f.client().webhooks().deliveries(ListWebhookDeliveriesParams.builder().finalOnly(false).build());
      assertEquals("true", f.call(0).query.get("includeArchived"));
      assertEquals("false", f.call(1).query.get("finalOnly"));
    }
  }

  @Test
  void pathSegmentsAreEncoded() {
    try (FakeApi f = new FakeApi(ok(map()))) {
      f.client().templates().get("a/b?c d");
      assertEquals("/v1/templates/a%2Fb%3Fc%20d", f.call(0).path);
    }
    assertThrows(IllegalArgumentException.class, () -> new SendClipper("k").templates().get(""));
  }

  @Test
  void theKeyAndBaseUrlFallBackToTheEnvironmentAndATrailingV1IsTolerated() {
    try (FakeApi f = new FakeApi(ok(list()))) {
      Map<String, String> env = map2("SENDCLIPPER_API_KEY", "sc_live_env", "SENDCLIPPER_BASE_URL", f.url() + "/v1/");
      SendClipper c = SendClipper.builder().env(env::get).build();
      c.domains().list();
      assertEquals(f.url(), c.getBaseUrl());
      assertEquals("/v1/domains", f.call(0).path);
      assertEquals("Bearer sc_live_env", f.call(0).header("Authorization"));
    }
    assertEquals("https://sendclipper.com", SendClipper.builder().env(n -> null).build().getBaseUrl());
  }

  static Map<String, String> map2(String... kv) {
    Map<String, String> m = new java.util.HashMap<>();
    for (int i = 0; i < kv.length; i += 2) {
      m.put(kv[i], kv[i + 1]);
    }
    return m;
  }

  @Test
  void defaultAndPerCallHeadersAreSent() {
    try (FakeApi f = new FakeApi(ok(list()))) {
      f.builder()
          .defaultHeader("X-App", "shop")
          .build()
          .domains()
          .list(RequestOptions.builder().header("X-Trace-Id", "t1").build());
      assertEquals("shop", f.call(0).header("X-App"));
      assertEquals("t1", f.call(0).header("X-Trace-Id"));
    }
  }

  @Test
  void aRedirectToAnotherHostDropsTheApiKeyAndOneToTheSameHostKeepsIt() {
    try (FakeApi other = new FakeApi(ok(list()));
        FakeApi f =
            new FakeApi(
                status(302, null).header("Location", other.url().replace("127.0.0.1", "localhost") + "/v1/domains"),
                status(307, null).header("Location", "/v1/domains/again"),
                ok(list()))) {
      SendClipper c = f.client();
      c.domains().list();
      assertNotNull(f.call(0).header("Authorization"));
      assertNull(other.call(0).header("Authorization"));
      c.domains().list();
      assertEquals("/v1/domains/again", f.call(2).path);
      assertEquals("Bearer sc_live_test", f.call(2).header("Authorization"));
    }
  }

  @Test
  void suppressionsRemoveSendsTheRecordedReasonAsTheDeleteBody() {
    try (FakeApi f = new FakeApi(ok(map("removed", true)))) {
      f.client().suppressions().remove("sup-1", "Customer asked us to resume mail");
      assertEquals("DELETE", f.call(0).method);
      assertEquals("/v1/suppressions/sup-1", f.call(0).path);
      assertEquals("Customer asked us to resume mail", f.call(0).json().get("reason").asText());
    }
  }

  @Test
  void suppressionsExportDropsPagingParams() {
    try (FakeApi f = new FakeApi(ok("email\n").header("Content-Type", "text/csv"))) {
      String csv =
          f.client()
              .suppressions()
              .export(ListSuppressionsParams.builder().reason("manual").cursor("c").limit(5).build());
      assertEquals("email\n", csv);
      assertEquals(map("reason", "manual"), f.call(0).query);
      assertEquals("*/*", f.call(0).header("Accept"));
    }
  }

  @Test
  void contactsImportIsMultipartWithNoIdempotencyKeyAndNeverRetried() {
    try (FakeApi f = new FakeApi(apiError(503, "unavailable", "down"))) {
      SendClipperException e =
          refused(
              () ->
                  f.client()
                      .contacts()
                      .importContacts(
                          ImportContactsParams.builder()
                              .file("name,email\nAda,ada@lumen.io\n".getBytes(StandardCharsets.UTF_8))
                              .filename("contacts.csv")
                              .onDuplicate("update")
                              .mapping(map("email", "email"))
                              .build()));
      assertEquals(503, e.getStatusCode());
      assertEquals(1, f.calls().size());
      Call c = f.call(0);
      assertNull(c.header("Idempotency-Key"));
      String type = c.header("Content-Type");
      assertTrue(type.startsWith("multipart/form-data; boundary="), type);
      String boundary = type.substring(type.indexOf('=') + 1);
      String body = c.bodyText();
      assertTrue(body.contains("Content-Disposition: form-data; name=\"onDuplicate\"\r\n\r\nupdate\r\n"), body);
      assertTrue(body.contains("name=\"mapping\"\r\n\r\n{\"email\":\"email\"}\r\n"), body);
      assertTrue(
          body.contains(
              "Content-Disposition: form-data; name=\"file\"; filename=\"contacts.csv\"\r\nContent-Type: text/csv\r\n\r\nname,email\nAda,ada@lumen.io\n\r\n"),
          body);
      assertFalse(body.contains("name=\"audienceId\""), "empty fields are left out");
      assertTrue(body.endsWith("--" + boundary + "--\r\n"), body);
    }
  }

  @Test
  void aVcfImportIsSentAsTextVcard() {
    try (FakeApi f = new FakeApi(ok(map("rows", 1)))) {
      JsonNode res =
          f.client()
              .contacts()
              .importPreview(
                  ImportContactsParams.builder()
                      .file("BEGIN:VCARD\r\nEND:VCARD\r\n".getBytes(StandardCharsets.UTF_8))
                      .filename("people.VCF")
                      .build());
      assertEquals(1, res.get("rows").asInt());
      assertEquals("/v1/contacts/import/preview", f.call(0).path);
      assertTrue(f.call(0).bodyText().contains("Content-Type: text/vcard"));
    }
  }

  @Test
  void templateAssetsUploadBase64EncodesBytes() {
    try (FakeApi f = new FakeApi(status(201, map("id", "a", "url", "https://x/y.png", "bytes", 3)))) {
      TemplateAsset a =
          f.client()
              .templates()
              .assets()
              .upload(UploadTemplateAssetParams.builder().mimeType("image/png").data(new byte[] {1, 2, 3}).name("logo").build());
      assertEquals("https://x/y.png", a.getUrl());
      assertEquals(3, a.getBytes());
      assertEquals(json("{\"base64\":\"AQID\",\"mimeType\":\"image/png\",\"name\":\"logo\"}"), f.call(0).json());
    }
  }

  @Test
  void textEndpointsReturnTheBodyAsAString() {
    try (FakeApi f = new FakeApi(ok("<html>hï</html>").header("Content-Type", "text/html"))) {
      assertEquals("<html>hï</html>", f.client().templates().html("tpl_1"));
    }
  }

  @Test
  void anAttachmentDownloadReturnsBytesTypeAndFilename() {
    try (FakeApi f =
        new FakeApi(
            ok("%PDF")
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=\"fallback.pdf\"; filename*=UTF-8''Rechnung%20M%C3%A4rz.pdf"),
            ok("x").header("Content-Disposition", "inline; filename=\"a \\\"b\\\".png\""))) {
      DownloadedFile file = f.client().mailboxes().messages().attachment("m1", "msg1", "p1");
      assertEquals("%PDF", new String(file.getContent(), StandardCharsets.UTF_8));
      assertEquals("application/pdf", file.getContentType());
      assertEquals("Rechnung März.pdf", file.getFilename());
      assertEquals("/v1/mailboxes/m1/messages/msg1/parts/p1", f.call(0).path);
      assertEquals("1", f.call(0).query.get("download"));
      assertEquals("*/*", f.call(0).header("Accept"));
      assertEquals("a \"b\".png", f.client().mailboxes().messages().attachment("m1", "msg1", "p2").getFilename());
    }
  }

  // ------------------------------------------------------------------- paging

  static Map<String, Object> row(String id) {
    Map<String, Object> r = new java.util.LinkedHashMap<>(SENT);
    r.put("id", id);
    return r;
  }

  @Test
  void emailsAllWalksEveryPageAndStopsOnTheLast() {
    try (FakeApi f =
        new FakeApi(
            ok(map("items", list(row("id-1"), row("id-2")), "total", 3)),
            ok(map("items", list(row("id-3")), "total", 3)))) {
      List<String> ids = new ArrayList<>();
      for (EmailRow e : f.client().emails().all(ListEmailsParams.builder().pageSize(2).page(7).build())) {
        ids.add(e.getId());
      }
      assertEquals(Arrays.asList("id-1", "id-2", "id-3"), ids);
      assertEquals(2, f.calls().size());
      assertEquals("1", f.call(0).query.get("page"));
      assertEquals("2", f.call(1).query.get("page"));
      assertEquals("2", f.call(1).query.get("pageSize"));
    }
  }

  @Test
  void anExactlyFullLastPageStopsOnTheTotal() {
    try (FakeApi f = new FakeApi(ok(map("items", list(row("a"), row("b")), "total", 2)))) {
      assertEquals(2, f.client().emails().all(ListEmailsParams.builder().pageSize(2).build()).stream().count());
      assertEquals(1, f.calls().size());
    }
  }

  @Test
  void stoppingEarlyStopsFetching() {
    try (FakeApi f =
        new FakeApi(ok(map("data", list(map("id", "a"), map("id", "b")), "page", map("cursor", "c1", "hasMore", true))))) {
      Iterator<Suppression> it = f.client().suppressions().all(null).iterator();
      assertEquals("a", it.next().getId());
      assertEquals(1, f.calls().size());
    }
    try (FakeApi f =
        new FakeApi(ok(map("data", list(map("id", "a"), map("id", "b")), "page", map("cursor", "c1", "hasMore", true))))) {
      assertEquals(1, f.client().suppressions().all(null).stream().limit(1).count());
      assertEquals(1, f.calls().size());
    }
  }

  @Test
  void suppressionsAllFollowsTheCursor() {
    try (FakeApi f =
        new FakeApi(
            ok(map("data", list(map("id", "1", "email", "a@x.io")), "page", map("cursor", "c1", "hasMore", true))),
            ok(map("data", list(map("id", "2", "email", "b@x.io")), "page", map("cursor", null, "hasMore", false))))) {
      List<String> seen =
          f.client()
              .suppressions()
              .all(ListSuppressionsParams.builder().reason("manual").cursor("ignored").build())
              .stream()
              .map(Suppression::getEmail)
              .collect(Collectors.toList());
      assertEquals(Arrays.asList("a@x.io", "b@x.io"), seen);
      assertFalse(f.call(0).query.containsKey("cursor"), "the first page carries no cursor");
      assertEquals("c1", f.call(1).query.get("cursor"));
      assertEquals("manual", f.call(1).query.get("reason"));
    }
  }

  @Test
  void eachIterationStartsAgainFromTheFirstPage() {
    Map<String, Object> page = map("data", list(map("id", "a")), "page", map("cursor", null, "hasMore", false));
    try (FakeApi f = new FakeApi(ok(page), ok(page))) {
      PageIterable<Suppression> all = f.client().suppressions().all(null);
      assertEquals(1, all.stream().count());
      assertEquals(1, all.stream().count());
      assertEquals(2, f.calls().size());
    }
  }

  @Test
  void aFailedPageThrowsFromTheIterator() {
    try (FakeApi f = new FakeApi(apiError(403, "api_key_not_accepted", "no"))) {
      Iterator<Contact> it = f.client().contacts().all(null).iterator();
      SendClipperException e = assertThrows(SendClipperException.class, it::hasNext);
      assertEquals("api_key_not_accepted", e.getName());
      assertEquals(403, e.getStatusCode());
    }
  }

  @Test
  void contactsAllStopsOnTheLastPage() {
    try (FakeApi f =
        new FakeApi(
            ok(map("items", list(map("id", "c1")), "totalPages", 2)),
            ok(map("items", list(map("id", "c2")), "totalPages", 2)))) {
      assertEquals(
          Arrays.asList("c1", "c2"),
          f.client().contacts().all(null).stream().map(Contact::getId).collect(Collectors.toList()));
      assertEquals("200", f.call(0).query.get("pageSize"));
    }
  }

  // ------------------------------------------------------------------- sandboxes

  @Test
  void sandboxWaitForPollsUntilAMatchThenReadsIt() {
    Map<String, Object> summary = map("id", "m2", "subject", "Hi", "trappedAt", "2026-10-16T10:00:00.000Z");
    Map<String, Object> full = map("id", "m2", "subject", "Hi", "trappedAt", "2026-10-16T10:00:00.000Z", "bodyHtml", "<p>Hi</p>", "hasRaw", true);
    try (FakeApi f = new FakeApi(ok(list()), ok(list(summary)), ok(full))) {
      SandboxMessage m =
          f.client()
              .sandboxes()
              .messages()
              .waitFor(
                  "sbx1",
                  WaitForOptions.builder().match(s -> s.getSubject().equals("Hi")).interval(Duration.ofMillis(1)).build());
      assertEquals("<p>Hi</p>", m.getBodyHtml());
      assertTrue(m.hasRaw());
      assertEquals(
          Arrays.asList("/v1/sandboxes/sbx1/messages", "/v1/sandboxes/sbx1/messages", "/v1/sandboxes/sbx1/messages/m2"),
          f.calls().stream().map(c -> c.path).collect(Collectors.toList()));
      assertEquals("50", f.call(0).query.get("limit"));
    }
  }

  @Test
  void sandboxWaitForEndsWithTimeout() {
    FakeApi.Reply[] replies = new FakeApi.Reply[400];
    for (int i = 0; i < replies.length; i++) {
      replies[i] = ok(list());
    }
    FakeApi f = new FakeApi(replies);
    try {
      SendClipperException e =
          refused(
              () ->
                  f.client()
                      .sandboxes()
                      .messages()
                      .waitFor("sbx1", WaitForOptions.builder().interval(Duration.ofMillis(5)).timeout(Duration.ofMillis(60)).build()));
      assertEquals("timeout", e.getName());
      assertEquals(0, e.getStatusCode());
      assertTrue(e.getMessage().startsWith("No matching message reached sandbox sbx1 within"), e.getMessage());
    } finally {
      f.close();
    }
  }

  // ------------------------------------------------------------------- misc

  @Test
  void webhookEventTypesTravelAsTheirWireNames() {
    try (FakeApi f = new FakeApi(status(201, map("id", "w1", "secret", "whsec_x", "events", list("email.delivered", "email.future"))))) {
      com.sendclipper.model.WebhookEndpointWithSecret w =
          f.client()
              .webhooks()
              .create(
                  com.sendclipper.model.CreateWebhookParams.builder()
                      .url("https://x.io/hook")
                      .events(WebhookEventType.EMAIL_DELIVERED, WebhookEventType.EMAIL_BOUNCED)
                      .build());
      assertEquals("whsec_x", w.getSecret());
      assertEquals(Arrays.asList(WebhookEventType.EMAIL_DELIVERED, WebhookEventType.UNKNOWN), w.getEvents());
      assertEquals(json("{\"url\":\"https://x.io/hook\",\"events\":[\"email.delivered\",\"email.bounced\"]}"), f.call(0).json());
    }
  }

  @Test
  void aTemplateCreateCarriesBlocks() {
    try (FakeApi f = new FakeApi(status(201, map("id", "t1", "blocks", list(map("type", "text", "text", "Hi")))))) {
      com.sendclipper.model.Template t =
          f.client()
              .templates()
              .create(
                  CreateTemplateParams.builder()
                      .name("welcome")
                      .block(map("type", "text", "text", "Hi {{first_name|there}}"))
                      .build());
      assertEquals("Hi", t.getBlocks().get(0).get("text"));
      assertEquals(
          json("{\"name\":\"welcome\",\"blocks\":[{\"type\":\"text\",\"text\":\"Hi {{first_name|there}}\"}]}"),
          f.call(0).json());
    }
  }

  @Test
  void setTagsReturnsTheTemplatesTags() {
    try (FakeApi f = new FakeApi(ok(map("tags", list(map("id", "g1", "name", "Billing", "color", "blue")))))) {
      assertEquals("Billing", f.client().templates().setTags("t1", Collections.singletonList("g1")).get(0).getName());
    }
  }
}
