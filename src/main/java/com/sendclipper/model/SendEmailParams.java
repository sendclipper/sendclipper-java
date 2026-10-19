package com.sendclipper.model;

import java.util.List;
import java.util.Map;

/**
 * One message. Give it a body (subject + html) or a published template (templateId or template, plus variables).
 *
 * <p>Make one with {@link #builder()}.
 */
public final class SendEmailParams {
  private String from;
  private String to;
  private String subject;
  private String html;
  private String text;
  private String templateId;
  private String template;
  private Map<String, Object> variables;
  private SendClass sendClass;
  private Boolean trackOpens;
  private Boolean trackClicks;
  private List<String> replyTo;
  private List<Attachment> attachments;

  private SendEmailParams() {}

  /**
   * Starts a builder.
   *
   * @return a new builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * A bare address on a verified domain: {@code hello@send.acme.io}. A display-name form is refused.
   *
   * @return the from
   */
  public String getFrom() {
    return from;
  }

  /**
   * ONE recipient. For several, use {@code batch().send(...)} with one item each.
   *
   * @return the to
   */
  public String getTo() {
    return to;
  }

  /**
   * The subject line.
   *
   * @return the subject
   */
  public String getSubject() {
    return subject;
  }

  /**
   * The HTML body.
   *
   * @return the html
   */
  public String getHtml() {
    return html;
  }

  /**
   * The plain-text alternative.
   *
   * @return the text
   */
  public String getText() {
    return text;
  }

  /**
   * A published template's public id ({@code tpl_...}) or uuid.
   *
   * @return the template id
   */
  public String getTemplateId() {
    return templateId;
  }

  /**
   * A published template's send name (its key, e.g. {@code welcome}). Use this or templateId.
   *
   * @return the template
   */
  public String getTemplate() {
    return template;
  }

  /**
   * Values for {@code {{tags}}} in a template, or in a hand-written HTML body.
   *
   * @return the variables
   */
  public Map<String, Object> getVariables() {
    return variables;
  }

  /**
   * {@code MARKETING} adds one-click unsubscribe and the postal-address footer.
   *
   * @return the send class
   */
  public SendClass getSendClass() {
    return sendClass;
  }

  /**
   * false leaves the open-tracking pixel out. Default true.
   *
   * @return the track opens
   */
  public Boolean getTrackOpens() {
    return trackOpens;
  }

  /**
   * false leaves links unwrapped. Default true.
   *
   * @return the track clicks
   */
  public Boolean getTrackClicks() {
    return trackClicks;
  }

  /**
   * The Reply-To header: 1 to 10 bare addresses. Accepted on a batch item too.
   *
   * @return the reply to
   */
  public List<String> getReplyTo() {
    return replyTo;
  }

  /**
   * Files sent with the message: 10 MiB per file, 25 MiB of files per message, 20 files. Not accepted on a batch item: the API fails that item with {@code attachments_not_batchable}.
   *
   * @return the attachments
   */
  public List<Attachment> getAttachments() {
    return attachments;
  }

  /** Builds {@link SendEmailParams}. A builder builds once. */
  public static final class Builder {
    private final SendEmailParams p = new SendEmailParams();
    private boolean built;

    private Builder() {}

    /**
     * A bare address on a verified domain: {@code hello@send.acme.io}. A display-name form is refused.
     *
     * @param from the from
     * @return this builder
     */
    public Builder from(String from) {
      check();
      p.from = from;
      return this;
    }
    /**
     * ONE recipient. For several, use {@code batch().send(...)} with one item each.
     *
     * @param to the to
     * @return this builder
     */
    public Builder to(String to) {
      check();
      p.to = to;
      return this;
    }
    /**
     * The subject line.
     *
     * @param subject the subject
     * @return this builder
     */
    public Builder subject(String subject) {
      check();
      p.subject = subject;
      return this;
    }
    /**
     * The HTML body.
     *
     * @param html the html
     * @return this builder
     */
    public Builder html(String html) {
      check();
      p.html = html;
      return this;
    }
    /**
     * The plain-text alternative.
     *
     * @param text the text
     * @return this builder
     */
    public Builder text(String text) {
      check();
      p.text = text;
      return this;
    }
    /**
     * A published template's public id ({@code tpl_...}) or uuid.
     *
     * @param templateId the template id
     * @return this builder
     */
    public Builder templateId(String templateId) {
      check();
      p.templateId = templateId;
      return this;
    }
    /**
     * A published template's send name (its key, e.g. {@code welcome}). Use this or templateId.
     *
     * @param template the template
     * @return this builder
     */
    public Builder template(String template) {
      check();
      p.template = template;
      return this;
    }
    /**
     * Values for {@code {{tags}}} in a template, or in a hand-written HTML body.
     *
     * @param variables the variables
     * @return this builder
     */
    public Builder variables(Map<String, Object> variables) {
      check();
      p.variables = variables;
      return this;
    }
    /**
     * {@code MARKETING} adds one-click unsubscribe and the postal-address footer.
     *
     * @param sendClass the send class
     * @return this builder
     */
    public Builder sendClass(SendClass sendClass) {
      check();
      p.sendClass = sendClass;
      return this;
    }
    /**
     * false leaves the open-tracking pixel out. Default true.
     *
     * @param trackOpens the track opens
     * @return this builder
     */
    public Builder trackOpens(Boolean trackOpens) {
      check();
      p.trackOpens = trackOpens;
      return this;
    }
    /**
     * false leaves links unwrapped. Default true.
     *
     * @param trackClicks the track clicks
     * @return this builder
     */
    public Builder trackClicks(Boolean trackClicks) {
      check();
      p.trackClicks = trackClicks;
      return this;
    }
    /**
     * The Reply-To header: 1 to 10 bare addresses. Accepted on a batch item too.
     *
     * @param replyTo the reply to
     * @return this builder
     */
    public Builder replyTo(List<String> replyTo) {
      check();
      p.replyTo = replyTo;
      return this;
    }
    /**
     * Files sent with the message: 10 MiB per file, 25 MiB of files per message, 20 files. Not accepted on a batch item: the API fails that item with {@code attachments_not_batchable}.
     *
     * @param attachments the attachments
     * @return this builder
     */
    public Builder attachments(List<Attachment> attachments) {
      check();
      p.attachments = attachments;
      return this;
    }

    /**
     * Sets one value for a {@code {{tag}}}, keeping the others.
     *
     * @param name the tag's name
     * @param value its value
     * @return this builder
     */
    public Builder variable(String name, Object value) {
      check();
      if (p.variables == null) {
        p.variables = new java.util.LinkedHashMap<>();
      }
      p.variables.put(name, value);
      return this;
    }

    /**
     * Sets the Reply-To addresses.
     *
     * @param addresses 1 to 10 bare addresses
     * @return this builder
     */
    public Builder replyTo(String... addresses) {
      return replyTo(java.util.Arrays.asList(addresses));
    }

    /**
     * Adds one attachment.
     *
     * @param attachment the file
     * @return this builder
     */
    public Builder attachment(Attachment attachment) {
      check();
      if (p.attachments == null) {
        p.attachments = new java.util.ArrayList<>();
      }
      p.attachments.add(attachment);
      return this;
    }

    /**
     * Builds the params.
     *
     * @return the params
     */
    public SendEmailParams build() {
      check();
      built = true;
      return p;
    }

    private void check() {
      if (built) {
        throw new IllegalStateException("this builder already built its SendEmailParams");
      }
    }
  }
}
