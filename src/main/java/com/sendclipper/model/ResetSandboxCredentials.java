package com.sendclipper.model;

/** The new credentials. The token is shown once. */
public class ResetSandboxCredentials extends ApiObject {
  private String username;
  private String password;
  private String apiToken;

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public String getApiToken() {
    return apiToken;
  }

}
