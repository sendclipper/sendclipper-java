package com.sendclipper;

import com.sendclipper.core.HttpCore;
import com.sendclipper.resources.ApiKeys;
import com.sendclipper.resources.Audiences;
import com.sendclipper.resources.Automations;
import com.sendclipper.resources.Batch;
import com.sendclipper.resources.Contacts;
import com.sendclipper.resources.Domains;
import com.sendclipper.resources.Emails;
import com.sendclipper.resources.Mailboxes;
import com.sendclipper.resources.Sandboxes;
import com.sendclipper.resources.Suppressions;
import com.sendclipper.resources.Templates;
import com.sendclipper.resources.Webhooks;
import com.sendclipper.resources.WorkspaceResource;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * The SendClipper API client. Make one and share it: it is safe for concurrent use.
 *
 * <pre>{@code
 * SendClipper client = new SendClipper("sc_live_...");   // or new SendClipper(): reads SENDCLIPPER_API_KEY
 *
 * SentEmail email = client.emails().send(SendEmailParams.builder()
 *     .from("hello@send.acme.io")
 *     .to("ada@lumen.io")
 *     .subject("Your receipt")
 *     .html("<p>Thanks for your order.</p>")
 *     .build());
 * }</pre>
 *
 * <p>Every method returns the decoded answer or throws a {@link SendClipperException} whose
 * {@code getName()} is SendClipper's machine-readable code. Network errors, 429 and 5xx answers are
 * retried with backoff, honouring Retry-After. Every JSON write carries an Idempotency-Key, and the
 * API answers a repeated key with the first result for 24 hours, so a retried send never goes out
 * twice.
 */
public final class SendClipper {
  /** Where requests go unless the builder or SENDCLIPPER_BASE_URL says otherwise. */
  public static final String DEFAULT_BASE_URL = "https://sendclipper.com";

  private final HttpCore core;
  private final Emails emails;
  private final Batch batch;
  private final Domains domains;
  private final Templates templates;
  private final Sandboxes sandboxes;
  private final Webhooks webhooks;
  private final Contacts contacts;
  private final Audiences audiences;
  private final Suppressions suppressions;
  private final Mailboxes mailboxes;
  private final Automations automations;
  private final WorkspaceResource workspace;
  private final ApiKeys apiKeys;

  /** A client that reads its key from SENDCLIPPER_API_KEY. */
  public SendClipper() {
    this(builder());
  }

  /**
   * A client with this API key and the default settings.
   *
   * @param apiKey an {@code sc_live_} key
   */
  public SendClipper(String apiKey) {
    this(builder().apiKey(apiKey));
  }

  private SendClipper(Builder b) {
    String key = b.apiKey;
    if (key == null || key.isEmpty()) {
      key = b.env.apply("SENDCLIPPER_API_KEY");
    }
    String base = b.baseUrl;
    if (base == null || base.isEmpty()) {
      base = b.env.apply("SENDCLIPPER_BASE_URL");
    }
    if (base == null || base.isEmpty()) {
      base = DEFAULT_BASE_URL;
    }
    HttpClient http = b.httpClient;
    if (http == null) {
      http =
          HttpClient.newBuilder()
              .followRedirects(HttpClient.Redirect.NEVER)
              .connectTimeout(b.timeout)
              .build();
    }
    this.core =
        new HttpCore(
            key,
            HttpCore.normaliseBaseUrl(base),
            http,
            b.timeout,
            b.maxRetries,
            b.defaultHeaders,
            b.sleeper);
    this.emails = new Emails(core);
    this.batch = new Batch(core);
    this.domains = new Domains(core);
    this.templates = new Templates(core);
    this.sandboxes = new Sandboxes(core);
    this.webhooks = new Webhooks(core);
    this.contacts = new Contacts(core);
    this.audiences = new Audiences(core);
    this.suppressions = new Suppressions(core);
    this.mailboxes = new Mailboxes(core);
    this.automations = new Automations(core);
    this.workspace = new WorkspaceResource(core);
    this.apiKeys = new ApiKeys(core);
  }

  /**
   * Starts a builder, for a base URL, timeout, retries or your own {@link HttpClient}.
   *
   * @return a new builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * The API origin requests go to, without /v1.
   *
   * @return the origin
   */
  public String getBaseUrl() {
    return core.baseUrl();
  }

  /**
   * Sends mail and reads the sent-mail log.
   *
   * @return the emails resource
   */
  public Emails emails() {
    return emails;
  }

  /**
   * Sends up to 500 messages in one request.
   *
   * @return the batch resource
   */
  public Batch batch() {
    return batch;
  }

  /**
   * Sending and receiving domains.
   *
   * @return the domains resource
   */
  public Domains domains() {
    return domains;
  }

  /**
   * Email templates, their tags and images.
   *
   * @return the templates resource
   */
  public Templates templates() {
    return templates;
  }

  /**
   * Sandboxes: inboxes that trap mail while you develop.
   *
   * @return the sandboxes resource
   */
  public Sandboxes sandboxes() {
    return sandboxes;
  }

  /**
   * Webhook endpoints and undelivered events.
   *
   * @return the webhooks resource
   */
  public Webhooks webhooks() {
    return webhooks;
  }

  /**
   * Contacts.
   *
   * @return the contacts resource
   */
  public Contacts contacts() {
    return contacts;
  }

  /**
   * Audiences.
   *
   * @return the audiences resource
   */
  public Audiences audiences() {
    return audiences;
  }

  /**
   * The suppression list.
   *
   * @return the suppressions resource
   */
  public Suppressions suppressions() {
    return suppressions;
  }

  /**
   * Addresses on receiving domains, and their mail.
   *
   * @return the mailboxes resource
   */
  public Mailboxes mailboxes() {
    return mailboxes;
  }

  /**
   * Automations.
   *
   * @return the automations resource
   */
  public Automations automations() {
    return automations;
  }

  /**
   * The key's workspace.
   *
   * @return the workspace resource
   */
  public WorkspaceResource workspace() {
    return workspace;
  }

  /**
   * The workspace's API keys.
   *
   * @return the API keys resource
   */
  public ApiKeys apiKeys() {
    return apiKeys;
  }

  /** Builds a {@link SendClipper} client. */
  public static final class Builder {
    private String apiKey;
    private String baseUrl;
    private HttpClient httpClient;
    private Duration timeout = Duration.ofSeconds(30);
    private int maxRetries = 2;
    private final Map<String, String> defaultHeaders = new LinkedHashMap<>();
    private HttpCore.Sleeper sleeper = d -> Thread.sleep(d.toMillis());
    private Function<String, String> env = System::getenv;

    private Builder() {}

    /**
     * Sets the API key. Without one the client reads SENDCLIPPER_API_KEY.
     *
     * @param apiKey an {@code sc_live_} key
     * @return this builder
     */
    public Builder apiKey(String apiKey) {
      this.apiKey = apiKey;
      return this;
    }

    /**
     * Points the client at another API origin (no /v1). The default is SENDCLIPPER_BASE_URL, then
     * {@value SendClipper#DEFAULT_BASE_URL}.
     *
     * @param baseUrl the origin
     * @return this builder
     */
    public Builder baseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
      return this;
    }

    /**
     * Sends requests through your own {@link HttpClient} (a proxy, an executor, TLS settings).
     * Build it with {@code followRedirects(Redirect.NEVER)}: the SDK follows redirects itself, so
     * that the API key never reaches another host.
     *
     * @param httpClient the client
     * @return this builder
     */
    public Builder httpClient(HttpClient httpClient) {
      this.httpClient = httpClient;
      return this;
    }

    /**
     * Sets the per-attempt timeout. Default 30 seconds.
     *
     * @param timeout a positive duration
     * @return this builder
     */
    public Builder timeout(Duration timeout) {
      if (timeout == null || timeout.isZero() || timeout.isNegative()) {
        throw new IllegalArgumentException("timeout must be positive");
      }
      this.timeout = timeout;
      return this;
    }

    /**
     * Sets how often a network error, 429 or 5xx is retried. Default 2.
     *
     * @param maxRetries 0 or more
     * @return this builder
     */
    public Builder maxRetries(int maxRetries) {
      this.maxRetries = Math.max(0, maxRetries);
      return this;
    }

    /**
     * Adds a header to every request.
     *
     * @param name the header name
     * @param value its value
     * @return this builder
     */
    public Builder defaultHeader(String name, String value) {
      this.defaultHeaders.put(name, value);
      return this;
    }

    /** For tests: replaces the wait between attempts. */
    Builder sleeper(HttpCore.Sleeper sleeper) {
      this.sleeper = sleeper;
      return this;
    }

    /** For tests: replaces the environment lookup. */
    Builder env(Function<String, String> env) {
      this.env = env;
      return this;
    }

    /**
     * Builds the client.
     *
     * @return the client
     */
    public SendClipper build() {
      return new SendClipper(this);
    }
  }
}
