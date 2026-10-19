package com.sendclipper.model;

/** Points a project at the sandbox. */
public class SandboxCredentials extends ApiObject {
  private String host;
  private int port;
  private String tls;
  private int maxRecipients;
  private String username;
  private String password;
  private long maxMessageBytes;
  private int retentionDays;
  private String apiEndpoint;
  private String inboxId;
  private String inboxAddress;
  private String inboundDomain;
  private boolean inboundLive;
  private String apiTokenHint;

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  /**
   * {@code required}, {@code optional} or {@code none}.
   *
   * @return the tls
   */
  public String getTls() {
    return tls;
  }

  public int getMaxRecipients() {
    return maxRecipients;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public long getMaxMessageBytes() {
    return maxMessageBytes;
  }

  public int getRetentionDays() {
    return retentionDays;
  }

  public String getApiEndpoint() {
    return apiEndpoint;
  }

  public String getInboxId() {
    return inboxId;
  }

  public String getInboxAddress() {
    return inboxAddress;
  }

  public String getInboundDomain() {
    return inboundDomain;
  }

  public boolean isInboundLive() {
    return inboundLive;
  }

  /**
   * The JSON token is hashed; only its hint can be read back.
   *
   * @return the api token hint
   */
  public String getApiTokenHint() {
    return apiTokenHint;
  }

}
