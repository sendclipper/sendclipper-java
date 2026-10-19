package com.sendclipper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sendclipper.model.Attachment;
import com.sendclipper.model.Contact;
import com.sendclipper.model.CreateContactParams;
import com.sendclipper.model.CreateSuppressionParams;
import com.sendclipper.model.CreateTemplateParams;
import com.sendclipper.model.CreateWebhookParams;
import com.sendclipper.model.Domain;
import com.sendclipper.model.EmailDetail;
import com.sendclipper.model.ImportContactsParams;
import com.sendclipper.model.ListContactsParams;
import com.sendclipper.model.ListEmailsParams;
import com.sendclipper.model.ListMailboxMessagesParams;
import com.sendclipper.model.ListSandboxMessagesParams;
import com.sendclipper.model.ListSuppressionsParams;
import com.sendclipper.model.ListWebhookDispatchesParams;
import com.sendclipper.model.Mailbox;
import com.sendclipper.model.MailboxMessageList;
import com.sendclipper.model.ResetSandboxCredentials;
import com.sendclipper.model.Sandbox;
import com.sendclipper.model.SandboxMessage;
import com.sendclipper.model.SandboxMessageSummary;
import com.sendclipper.model.SandboxParams;
import com.sendclipper.model.SendBatchResponse;
import com.sendclipper.model.SendEmailParams;
import com.sendclipper.model.SentEmail;
import com.sendclipper.model.SuppressionCreated;
import com.sendclipper.model.Template;
import com.sendclipper.model.TemplatePreview;
import com.sendclipper.model.TemplateSummary;
import com.sendclipper.model.UpdateWebhookParams;
import com.sendclipper.model.WaitForOptions;
import com.sendclipper.model.WebhookEndpoint;
import com.sendclipper.model.WebhookEndpointWithSecret;
import com.sendclipper.model.WebhookEvent;
import com.sendclipper.model.WebhookEventType;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Live end-to-end: this SDK against a real SendClipper API, with an sc_live_ key minted for the
 * run.
 *
 * <pre>LIVE_API_URL=http://localhost:4000 mvn -Plive test</pre>
 *
 * <p>It refuses anything that is not localhost: it creates and deletes data and sends mail through
 * whatever transport the API has, which must be the local SES emulator (checked on /v1/health
 * first). Skipped when LIVE_API_URL is unset.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.MethodName.class)
class LiveTest {
  static final ObjectMapper MAPPER = new ObjectMapper();
  final HttpClient http = HttpClient.newHttpClient();

  String api;
  String jwt;
  String keyId;
  String run;
  String from;
  SendClipper c;

  @BeforeAll
  void setUp() throws Exception {
    api = System.getenv("LIVE_API_URL");
    assumeTrue(api != null && !api.isEmpty(), "LIVE_API_URL is not set");
    String host = URI.create(api).getHost();
    if (!host.equals("localhost") && !host.equals("127.0.0.1") && !host.equals("host.docker.internal")) {
      throw new IllegalStateException("the live test runs against a local API only, not " + host);
    }
    run = "sdkjava" + Long.toHexString(System.currentTimeMillis());
    JsonNode health = raw("GET", "/health", null);
    JsonNode backends = health.path("backends");
    if (!backends.path("sesSend").asText().equals("local")
        || !backends.path("objectStore").asText().equals("filesystem")) {
      throw new IllegalStateException("refusing a non-local API: " + backends);
    }
    String email = System.getenv().getOrDefault("LIVE_EMAIL", "enis@sendclipper.dev");
    String password = System.getenv().getOrDefault("LIVE_PASSWORD", "password");
    jwt = raw("POST", "/auth/login", Map.of("email", email, "password", password)).path("token").asText();
    JsonNode key = raw("POST", "/api-keys", Map.of("name", run + "-full", "permission", "full"));
    keyId = key.path("id").asText();
    c = SendClipper.builder().apiKey(key.path("token").asText()).baseUrl(api).build();
    for (Domain d : c.domains().list()) {
      if (d.getStatus().equals("verified") && !d.getName().endsWith(".example.com")) {
        from = "sdk-live@" + d.getName();
        break;
      }
    }
    assertNotNull(from, "the seeded workspace needs a verified domain");
  }

  @AfterAll
  void tearDown() throws Exception {
    if (keyId != null) {
      raw("DELETE", "/api-keys/" + keyId, null);
    }
  }

  JsonNode raw(String method, String path, Object body) throws Exception {
    HttpRequest.Builder b =
        HttpRequest.newBuilder(URI.create(api + "/v1" + path))
            .header("Content-Type", "application/json")
            .method(
                method,
                body == null
                    ? HttpRequest.BodyPublishers.noBody()
                    : HttpRequest.BodyPublishers.ofByteArray(MAPPER.writeValueAsBytes(body)));
    if (jwt != null) {
      b.header("Authorization", "Bearer " + jwt);
    }
    HttpResponse<String> res = http.send(b.build(), HttpResponse.BodyHandlers.ofString());
    if (res.statusCode() >= 300) {
      throw new AssertionError(method + " " + path + " -> " + res.statusCode() + " " + res.body());
    }
    return res.body().isEmpty() ? MAPPER.nullNode() : MAPPER.readTree(res.body());
  }

  @Test
  void a_everyListAndReadDecodesIntoItsTypedModel() {
    assertFalse(c.workspace().get().getPublicId().isEmpty());
    assertTrue(c.apiKeys().list().stream().anyMatch(k -> k.getId().equals(keyId)));
    List<Domain> domains = c.domains().list();
    assertNotNull(c.domains().records(domains.get(0).getId()).getDomain());
    assertTrue(c.emails().stats().getTotal() >= 0);
    c.emails().list(ListEmailsParams.builder().pageSize(5).build());
    List<TemplateSummary> tpls = c.templates().list(null);
    if (!tpls.isEmpty()) {
      assertNotNull(c.templates().get(tpls.get(0).getId()).getBlocks());
    }
    c.templates().tags().list();
    c.templates().assets().list();
    c.templates().projects();
    List<Sandbox> boxes = c.sandboxes().list();
    if (!boxes.isEmpty()) {
      c.sandboxes().credentials(boxes.get(0).getId());
      List<SandboxMessageSummary> msgs =
          c.sandboxes().messages().list(boxes.get(0).getId(), ListSandboxMessagesParams.builder().limit(5).build());
      if (!msgs.isEmpty()) {
        c.sandboxes().messages().get(boxes.get(0).getId(), msgs.get(0).getId());
      }
    }
    c.webhooks().list();
    c.webhooks().dispatches().list(ListWebhookDispatchesParams.builder().state("all").limit(5).build());
    c.contacts().list(ListContactsParams.builder().pageSize(5).build());
    c.audiences().list();
    c.suppressions().list(ListSuppressionsParams.builder().limit(5).build());
    for (Mailbox mb : c.mailboxes().list()) {
      MailboxMessageList page =
          c.mailboxes().messages().list(mb.getId(), ListMailboxMessagesParams.builder().limit(5).build());
      if (!page.getItems().isEmpty()) {
        assertNotNull(c.mailboxes().messages().get(mb.getId(), page.getItems().get(0).getId()).getId());
        break;
      }
    }
  }

  @Test
  void b_sendThenGetAndARepeatedIdempotencyKeyAnswersTheFirstSend() {
    SendEmailParams p =
        SendEmailParams.builder()
            .from(from)
            .to("ada+" + run + "@lumen.io")
            .subject("SDK live " + run + " Grüße <ok>")
            .html("<p>Hello from Java</p>")
            .text("Hello from Java")
            .replyTo("support+" + run + "@lumen.io")
            .attachment(Attachment.of("notes.txt", "hi".getBytes(StandardCharsets.UTF_8)))
            .build();
    RequestOptions key = RequestOptions.builder().idempotencyKey(run + "-welcome").build();
    SentEmail first = c.emails().send(p, key);
    AtomicReference<HttpHeaders> h = new AtomicReference<>();
    SentEmail again =
        c.emails()
            .send(p, RequestOptions.builder().idempotencyKey(run + "-welcome").onResponseHeaders(h::set).build());
    assertEquals(first.getId(), again.getId());
    assertEquals("true", h.get().firstValue("Idempotency-Replayed").orElse(null));
    EmailDetail d = c.emails().get(first.getId());
    assertEquals(p.getSubject(), d.getSubject());
    assertNotNull(d.getEvents());
  }

  @Test
  void c_anApiRefusalIsATypedExceptionWithTheApisCode() {
    SendClipperException e =
        assertThrows(
            SendClipperException.class,
            () ->
                c.emails()
                    .send(SendEmailParams.builder().from("nobody@not-a-domain-we-own.test").to("a@lumen.io").subject("s").html("h").build()));
    assertEquals(403, e.getStatusCode());
    assertFalse(e.getName().isEmpty());
    e =
        assertThrows(
            SendClipperException.class,
            () -> c.emails().send(SendEmailParams.builder().from(from).to("not-an-address").subject("s").html("h").build()));
    assertEquals("validation_failed", e.getName());
  }

  @Test
  void d_batchAcceptsAndRefusesItemsIndependently() {
    SendBatchResponse res =
        c.batch()
            .send(
                Arrays.asList(
                    SendEmailParams.builder().from(from).to("b1+" + run + "@lumen.io").subject("batch 1").html("<p>1</p>").build(),
                    SendEmailParams.builder().from(from).to("b2+" + run + "@lumen.io").templateId("tpl_doesnotexist0").build()));
    assertEquals(2, res.getSummary().getSubmitted());
    assertTrue(res.getData().get(0).isAccepted());
    assertEquals("failed", res.getData().get(1).getStatus());
  }

  @Test
  void e_templatesCreatePreviewPublishSendByIdRemove() {
    Template tpl =
        c.templates()
            .create(
                CreateTemplateParams.builder()
                    .name(run + " welcome")
                    .subject("Welcome {{first_name|there}}")
                    .block(Map.of("type", "text", "text", "Hi {{first_name|there}}, welcome aboard."))
                    .build());
    try {
      TemplatePreview p = c.templates().preview(tpl.getId(), Map.of("first_name", "Ada"));
      assertEquals("Welcome Ada", p.getSubject());
      assertEquals("Welcome there", c.templates().preview(tpl.getId(), null).getSubject());
      c.templates().publish(tpl.getId());
      SentEmail sent =
          c.emails()
              .send(
                  SendEmailParams.builder()
                      .from(from)
                      .to("t+" + run + "@lumen.io")
                      .templateId(tpl.getPublicId())
                      .variable("first_name", "Ada")
                      .build());
      assertEquals("Welcome Ada", sent.getSubject());
      assertTrue(c.templates().html(tpl.getId()).contains("Hi"));
    } finally {
      c.templates().remove(tpl.getId());
    }
  }

  @Test
  void f_sandboxesResetCredentialsSendWithTheTokenWaitRaw() {
    Sandbox box = c.sandboxes().create(SandboxParams.builder().name(run + "-box").build());
    try {
      ResetSandboxCredentials creds = c.sandboxes().resetCredentials(box.getId());
      assertTrue(creds.getApiToken().startsWith("sc_sbx_"));
      String subject = "trapped " + run;
      c.sandboxes()
          .send(
              creds.getApiToken(),
              SendEmailParams.builder().from("app@example.com").to("someone@example.com").subject(subject).html("<p>trap</p>").build());
      SandboxMessage m =
          c.sandboxes()
              .messages()
              .waitFor(
                  box.getId(),
                  WaitForOptions.builder()
                      .match(s -> s.getSubject().equals(subject))
                      .interval(Duration.ofMillis(300))
                      .timeout(Duration.ofSeconds(15))
                      .build());
      assertTrue(c.sandboxes().messages().raw(box.getId(), m.getId()).contains("Subject: " + subject));
    } finally {
      c.sandboxes().remove(box.getId());
    }
  }

  @Test
  void g_webhooksTheSecretComesOnceAndVerifyAcceptsIt() {
    WebhookEndpointWithSecret hook =
        c.webhooks()
            .create(
                CreateWebhookParams.builder()
                    .url("http://127.0.0.1:9/sdk-live-java")
                    .events(WebhookEventType.EMAIL_DELIVERED, WebhookEventType.EMAIL_BOUNCED)
                    .allowInsecureHttp(true)
                    .build());
    try {
      byte[] body =
          "{\"id\":\"evt_live\",\"event\":\"email.delivered\",\"createdAt\":\"2026-10-16T10:00:00Z\",\"data\":{}}"
              .getBytes(StandardCharsets.UTF_8);
      Map<String, List<String>> headers =
          Map.of(Webhook.SIGNATURE_HEADER, List.of(Webhook.signatureHeader(hook.getSecret(), body)));
      WebhookEvent ev = c.webhooks().verify(body, headers, hook.getSecret());
      assertEquals("evt_live", ev.getId());
      WebhookEndpoint up =
          c.webhooks().update(hook.getId(), UpdateWebhookParams.builder().events(WebhookEventType.EMAIL_OPENED).build());
      assertEquals(Collections.singletonList(WebhookEventType.EMAIL_OPENED), up.getEvents());
      assertNotEquals(hook.getSecret(), c.webhooks().rotateSecret(hook.getId()).getSecret());
    } finally {
      c.webhooks().remove(hook.getId());
    }
  }

  @Test
  void h_suppressionsAddFindRemoveWithAReason() {
    String email = "blocked+" + run + "@lumen.io";
    SuppressionCreated created =
        c.suppressions().create(CreateSuppressionParams.builder().email(email).note("sdk live test").build());
    assertTrue(created.isCreated());
    assertTrue(
        c.suppressions().all(ListSuppressionsParams.builder().q(email).build()).stream()
            .anyMatch(s -> s.getEmail().equals(email)));
    c.suppressions().remove(created.getRow().getId(), "Removed by the Java SDK live test run");
  }

  @Test
  void i_contactsAnImportPreviewAndIterators() {
    Contact ct =
        c.contacts().create(CreateContactParams.builder().name("SDK Live").email("contact+" + run + "@lumen.io").build());
    assertEquals("contact+" + run + "@lumen.io", ct.getEmail());
    JsonNode preview =
        c.contacts()
            .importPreview(
                ImportContactsParams.builder()
                    .file(("name,email\nJava Live,java+" + run + "@lumen.io\n").getBytes(StandardCharsets.UTF_8))
                    .build());
    assertTrue(preview.size() > 0);
    long n =
        c.emails().all(ListEmailsParams.builder().search(run).pageSize(2).build()).stream()
            .peek(e -> assertFalse(e.getId().isEmpty()))
            .count();
    assertTrue(n >= 3, "expected this run's sends in the log, saw " + n);
  }
}
