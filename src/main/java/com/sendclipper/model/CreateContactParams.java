package com.sendclipper.model;

/**
 * Adds a contact.
 *
 * <p>Make one with {@link #builder()}.
 */
public final class CreateContactParams {
  private String name;
  private String email;
  private String audienceId;
  private String status;

  private CreateContactParams() {}

  /**
   * Starts a builder.
   *
   * @return a new builder
   */
  public static Builder builder() {
    return new Builder();
  }

  public String getName() {
    return name;
  }

  public String getEmail() {
    return email;
  }

  public String getAudienceId() {
    return audienceId;
  }

  public String getStatus() {
    return status;
  }

  /** Builds {@link CreateContactParams}. A builder builds once. */
  public static final class Builder {
    private final CreateContactParams p = new CreateContactParams();
    private boolean built;

    private Builder() {}

    public Builder name(String name) {
      check();
      p.name = name;
      return this;
    }
    public Builder email(String email) {
      check();
      p.email = email;
      return this;
    }
    public Builder audienceId(String audienceId) {
      check();
      p.audienceId = audienceId;
      return this;
    }
    public Builder status(String status) {
      check();
      p.status = status;
      return this;
    }

    /**
     * Builds the params.
     *
     * @return the params
     */
    public CreateContactParams build() {
      check();
      built = true;
      return p;
    }

    private void check() {
      if (built) {
        throw new IllegalStateException("this builder already built its CreateContactParams");
      }
    }
  }
}
