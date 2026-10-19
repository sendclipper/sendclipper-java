package com.sendclipper.model;

/**
 * Suppresses an address (reason {@code manual}).
 *
 * <p>Make one with {@link #builder()}.
 */
public final class CreateSuppressionParams {
  private String email;
  private String note;

  private CreateSuppressionParams() {}

  /**
   * Starts a builder.
   *
   * @return a new builder
   */
  public static Builder builder() {
    return new Builder();
  }

  public String getEmail() {
    return email;
  }

  public String getNote() {
    return note;
  }

  /** Builds {@link CreateSuppressionParams}. A builder builds once. */
  public static final class Builder {
    private final CreateSuppressionParams p = new CreateSuppressionParams();
    private boolean built;

    private Builder() {}

    public Builder email(String email) {
      check();
      p.email = email;
      return this;
    }
    public Builder note(String note) {
      check();
      p.note = note;
      return this;
    }

    /**
     * Builds the params.
     *
     * @return the params
     */
    public CreateSuppressionParams build() {
      check();
      built = true;
      return p;
    }

    private void check() {
      if (built) {
        throw new IllegalStateException("this builder already built its CreateSuppressionParams");
      }
    }
  }
}
