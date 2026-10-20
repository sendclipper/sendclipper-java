# SendClipper Java SDK

The official Java library for the [SendClipper](https://sendclipper.com) email API.

Maven:

```xml
<dependency>
  <groupId>com.sendclipper</groupId>
  <artifactId>sendclipper-java</artifactId>
  <version>0.1.0</version>
</dependency>
```

Gradle (`build.gradle.kts`; the same line works in a Groovy `build.gradle`):

```kotlin
dependencies {
    implementation("com.sendclipper:sendclipper-java:0.1.0")
}
```

In a multi-module build, add it to the module that sends mail, not to the aggregator.

Java 11 or newer. It sends through the JDK's own `java.net.http` client and depends on Jackson only. API reference on [javadoc.io](https://javadoc.io/doc/com.sendclipper/sendclipper-java).

## Setup

Create an API key in the dashboard under **API keys**.

```java
import com.sendclipper.SendClipper;

SendClipper client = new SendClipper("sc_live_xxxxxxxxxxxxxxxx");
// or: new SendClipper()  reads SENDCLIPPER_API_KEY
```

A client is thread-safe: make one and share it (a static field, or a Spring `@Bean`).

## Send an email

```java
import com.sendclipper.SendClipperException;
import com.sendclipper.model.SendEmailParams;
import com.sendclipper.model.SentEmail;

try {
  SentEmail email = client.emails().send(SendEmailParams.builder()
      .from("hello@send.acme.io")
      .to("ada@lumen.io")
      .subject("Your receipt")
      .html("<p>Thanks for your order.</p>")
      .text("Thanks for your order.")
      .build());
  System.out.println("Email " + email.getId() + " accepted");
} catch (SendClipperException e) {
  System.err.println(e.getName() + ": " + e.getMessage()); // e.g. domain_not_verified
}
```

`from` must be a bare address on a domain you verified in SendClipper. Each message has one recipient in `to`; for several, use `batch().send(...)`.

## Reply-To and attachments

```java
import com.sendclipper.model.Attachment;
import java.nio.file.Path;

client.emails().send(SendEmailParams.builder()
    .from("bookings@acme.io")
    .to("ada@lumen.io")
    .subject("Your booking")
    .html("<p>Your confirmation is attached.</p>")
    .replyTo("support@acme.io")
    .attachment(Attachment.fromPath(Path.of("booking-42.pdf")))
    .build());
```

`replyTo` takes 1 to 10 addresses. `Attachment.of(filename, bytes)` and `Attachment.of(filename, bytes, contentType)` take bytes you already hold; the SDK base64-encodes them for you, and the content type is guessed from the filename when you leave it out. Limits: 10 MiB per file, 25 MiB of files per message, and your sending provider's own. Attachments are not accepted on a batch item; send those emails on their own.

## Send with a template

Design and publish a template in the dashboard, then send it by id or by its send name:

```java
client.emails().send(SendEmailParams.builder()
    .from("hello@send.acme.io")
    .to("ada@lumen.io")
    .templateId("tpl_9f2b7c10ad42") // or .template("welcome")
    .variable("first_name", "Ada")
    .build());
```

A tag with no value and no `{{tag|fallback}}` is refused by name (`template_variables_missing`), never sent blank.

## Send a batch

Up to 500 messages in one request. Each item is accepted or refused on its own:

```java
import com.sendclipper.model.BatchResult;
import com.sendclipper.model.SendBatchResponse;
import java.util.List;

SendBatchResponse res = client.batch().send(List.of(
    SendEmailParams.builder().from("hello@send.acme.io").to("ada@lumen.io").subject("Hi Ada").html("<p>Hi</p>").build(),
    SendEmailParams.builder().from("hello@send.acme.io").to("alan@lumen.io").templateId("tpl_9f2b7c10ad42").variable("first_name", "Alan").build()));
for (BatchResult item : res.getData()) {
  if (!item.isAccepted()) {
    System.err.println(item.getIndex() + " " + item.getError().getCode());
  }
}
```

## Errors

Every method returns the decoded answer or throws a `SendClipperException`, which is unchecked. A refusal from the API and a request that never got an answer are both one:

| Method | |
|---|---|
| `getName()` | SendClipper's code: `validation_failed`, `rate_limited`, `network_error`, ... |
| `getMessage()` | A sentence for a person |
| `getStatusCode()` | The HTTP status, `0` when no answer arrived (network, timeout, interrupted) |
| `getDetail()` | Code-specific facts, as a Jackson `JsonNode` (or null) |
| `getHeaders()` | The response headers, null when no answer arrived |

A `network_error` carries the transport error as its cause. An interrupted thread stops at once, keeps its interrupt flag and throws a `network_error`. For a successful call's headers, pass `RequestOptions.builder().onResponseHeaders(h -> ...)`.

## Retries and idempotency

Network errors, `429` and `5xx` answers are retried twice with backoff, honouring `Retry-After`. Every JSON write carries an `Idempotency-Key`, and the API answers a repeated key with the first result for 24 hours. A retried send therefore never goes out twice.

To make your own retries safe as well (a job that runs again after a crash), pass a key that names the logical operation:

```java
client.emails().send(params, RequestOptions.builder()
    .idempotencyKey("order-" + order.getId() + "-receipt")
    .build());
```

## Options

Every method has an overload whose last argument is a `RequestOptions`:

| Builder method | Meaning |
|---|---|
| `idempotencyKey(key)` | Your own key for a write |
| `timeout(duration)` | Per-attempt timeout (default 30 s) |
| `maxRetries(n)` | Retries for this call (default 2) |
| `header(name, value)` | An extra request header |
| `onResponseHeaders(consumer)` | Receive the response headers |

Client options:

```java
SendClipper client = SendClipper.builder()
    .apiKey("sc_live_...")
    .baseUrl("https://sendclipper.com") // also read from SENDCLIPPER_BASE_URL
    .timeout(Duration.ofSeconds(10))
    .maxRetries(3)
    .defaultHeader("X-App", "shop")
    .httpClient(myHttpClient) // build it with followRedirects(Redirect.NEVER)
    .build();
```

The SDK follows redirects itself so that the API key never reaches another host; a `HttpClient` of your own should therefore not follow them.

## Iterate

Lists that span pages have an `all` method that returns a lazy `PageIterable`. Loop over it, or stream it:

```java
for (EmailRow email : client.emails().all(ListEmailsParams.builder().status(EmailStatus.BOUNCED).build())) {
  System.out.println(email.getTo());
}

long manual = client.suppressions().all(ListSuppressionsParams.builder().reason("manual").build()).stream().count();
```

Pages are fetched as you go, so stopping early fetches no more. A page that fails throws its `SendClipperException` from the loop.

## Verify webhooks

SendClipper signs each delivery. Verify it against the **raw** body, before you parse it. The headers can be any `Map` of names to values: Spring's `HttpHeaders`, the JDK server's `Headers`, or your own:

```java
import com.sendclipper.Webhook;
import com.sendclipper.WebhookVerificationException;
import com.sendclipper.model.WebhookEvent;

@PostMapping("/hooks/sendclipper")
ResponseEntity<Void> hook(@RequestBody byte[] body, @RequestHeader HttpHeaders headers) {
  try {
    WebhookEvent event = Webhook.verify(body, headers, System.getenv("SENDCLIPPER_WEBHOOK_SECRET"));
    // event.getId() is stable across retries: deduplicate on it
    log.info("{} {}", event.getEventName(), event.getData().getTo());
    return ResponseEntity.ok().build();
  } catch (WebhookVerificationException e) {
    return ResponseEntity.badRequest().build();
  }
}
```

A servlet that holds only the header's value can call `Webhook.verifySignature(body, request.getHeader(Webhook.SIGNATURE_HEADER), secret)`.

A refusal is a `WebhookVerificationException` whose `getReason()` says which check failed. During a secret rotation a delivery carries two signatures and either one is accepted. A delivery signed more than five minutes away from now is refused; `Webhook.verifier(secret).tolerance(...)` changes that, and `.clock(...)` fixes the time in a test. `event.getData().getRaw()` holds every field of the event's data, and `getEvent()` is `UNKNOWN` (with `getEventName()` still set) for an event type this version does not know yet.

To test your handler locally, `Webhook.signatureHeader(secret, rawBody)` builds a valid header.

## Sandboxes

A sandbox traps mail while you develop: nothing is delivered.

```java
Sandbox box = client.sandboxes().create(SandboxParams.builder().name("my-app-dev").build());
SandboxCredentials creds = client.sandboxes().credentials(box.getId()); // SMTP host, port, username, password

// Or send through the sandbox's JSON door with its own token, using the same fields as emails().send():
ResetSandboxCredentials token = client.sandboxes().resetCredentials(box.getId());
client.sandboxes().send(token.getApiToken(), SendEmailParams.builder()
    .from("app@example.com").to("test@example.com").subject("Hi").html("<p>Hi</p>").build());

// In a test: wait for the message to arrive
SandboxMessage msg = client.sandboxes().messages().waitFor(box.getId(), WaitForOptions.builder()
    .match(m -> m.getSubject().equals("Hi"))
    .timeout(Duration.ofSeconds(30))
    .build());
```

## Everything else

| Resource | Methods |
|---|---|
| `emails()` | `send`, `sendBatch`, `get`, `list`, `all`, `stats` |
| `batch()` | `send` |
| `domains()` | `list`, `create`, `records`, `verify`, `enableReceiving`, `disableReceiving`, `setRegion`, `deletionPreview`, `remove` |
| `templates()` | `list`, `get`, `html`, `create`, `update`, `remove`, `duplicate`, `publish`, `unpublish`, `preview`, `setTags`, `projects` |
| `templates().tags()` | `list`, `create`, `update`, `remove` |
| `templates().assets()` | `list`, `upload` |
| `sandboxes()` | `list`, `get`, `create`, `update`, `remove`, `credentials`, `resetCredentials`, `send` |
| `sandboxes().messages()` | `list`, `get`, `raw`, `remove`, `clear`, `markAllRead`, `waitFor` |
| `webhooks()` | `list`, `create`, `update`, `remove`, `rotateSecret`, `deliveries`, `verify` |
| `webhooks().dispatches()` | `list`, `all`, `get`, `replay`, `replayAll` |
| `contacts()` | `list`, `all`, `create`, `importContacts`, `importPreview`, `export` |
| `audiences()` | `list` |
| `suppressions()` | `list`, `all`, `create`, `remove`, `export` |
| `mailboxes()` | `list`, `get`, `create`, `update`, `archive` |
| `mailboxes().messages()` | `list`, `all`, `get`, `attachment` |
| `automations()` | `enroll` |
| `workspace()` | `get` |
| `apiKeys()` | `list` |

Params are built with `XxxParams.builder()...build()`; responses are models with getters, and fields the API adds later are ignored rather than breaking decoding. Management routes need a key with **Full access**. A sending-only key can call `emails().send` and `batch().send`.

Full API reference: <https://sendclipper.com/docs>.

## Other languages

SendClipper has an official SDK for each of these languages, all calling the same API with the same conventions.

| Language | Package | Registry | Repository |
|---|---|---|---|
| Node.js | `@sendclipper/node` | [npm](https://www.npmjs.com/package/@sendclipper/node) | [sendclipper/sendclipper-node](https://github.com/sendclipper/sendclipper-node) |
| Python | `sendclipper` | [PyPI](https://pypi.org/project/sendclipper/) | [sendclipper/sendclipper-python](https://github.com/sendclipper/sendclipper-python) |
| PHP | `sendclipper/sendclipper-php` | [Packagist](https://packagist.org/packages/sendclipper/sendclipper-php) | [sendclipper/sendclipper-php](https://github.com/sendclipper/sendclipper-php) |
| Go | `github.com/sendclipper/sendclipper-go` | [pkg.go.dev](https://pkg.go.dev/github.com/sendclipper/sendclipper-go) | [sendclipper/sendclipper-go](https://github.com/sendclipper/sendclipper-go) |
| Ruby | `sendclipper` | [RubyGems](https://rubygems.org/gems/sendclipper) | [sendclipper/sendclipper-ruby](https://github.com/sendclipper/sendclipper-ruby) |
| Rust | `sendclipper` | [crates.io](https://crates.io/crates/sendclipper) | [sendclipper/sendclipper-rust](https://github.com/sendclipper/sendclipper-rust) |
| Java | `com.sendclipper:sendclipper-java` | [Maven Central](https://central.sonatype.com/artifact/com.sendclipper/sendclipper-java) | this library |

Documentation for all of them: <https://sendclipper.com/docs>.

## License

MIT
