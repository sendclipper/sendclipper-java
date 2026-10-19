package com.sendclipper.model;

import java.util.List;

/** One trapped message in full. */
public class SandboxMessage extends SandboxMessageSummary {
  private List<String> ccAddresses;
  private String envelopeFrom;
  private List<String> envelopeTo;
  private String bodyHtml;
  private String bodyText;
  private List<Header> headers;
  private String transport;
  private String clientIp;
  private boolean hasRaw;

  public List<String> getCcAddresses() {
    return ccAddresses;
  }

  /**
   * The SMTP envelope sender, recorded apart from the headers.
   *
   * @return the envelope from
   */
  public String getEnvelopeFrom() {
    return envelopeFrom;
  }

  /**
   * The SMTP envelope recipients (a Bcc recipient is only here).
   *
   * @return the envelope to
   */
  public List<String> getEnvelopeTo() {
    return envelopeTo;
  }

  public String getBodyHtml() {
    return bodyHtml;
  }

  /**
   * null when the message carried no text/plain part.
   *
   * @return the body text
   */
  public String getBodyText() {
    return bodyText;
  }

  public List<Header> getHeaders() {
    return headers;
  }

  public String getTransport() {
    return transport;
  }

  public String getClientIp() {
    return clientIp;
  }

  public boolean hasRaw() {
    return hasRaw;
  }

  /** One message header. */
  public static class Header extends ApiObject {
    private String name;
    private String value;

    public String getName() {
      return name;
    }

    public String getValue() {
      return value;
    }
  }
}
