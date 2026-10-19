package com.sendclipper.model;

/**
 * Makes an address.
 *
 * <p>Make one with {@link #builder()}.
 */
public final class CreateMailboxParams {
  private String domainId;
  private String localPart;
  private String displayName;
  private String color;
  private Boolean catchAll;

  private CreateMailboxParams() {}

  /**
   * Starts a builder.
   *
   * @return a new builder
   */
  public static Builder builder() {
    return new Builder();
  }

  public String getDomainId() {
    return domainId;
  }

  public String getLocalPart() {
    return localPart;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getColor() {
    return color;
  }

  public Boolean getCatchAll() {
    return catchAll;
  }

  /** Builds {@link CreateMailboxParams}. A builder builds once. */
  public static final class Builder {
    private final CreateMailboxParams p = new CreateMailboxParams();
    private boolean built;

    private Builder() {}

    public Builder domainId(String domainId) {
      check();
      p.domainId = domainId;
      return this;
    }
    public Builder localPart(String localPart) {
      check();
      p.localPart = localPart;
      return this;
    }
    public Builder displayName(String displayName) {
      check();
      p.displayName = displayName;
      return this;
    }
    public Builder color(String color) {
      check();
      p.color = color;
      return this;
    }
    public Builder catchAll(Boolean catchAll) {
      check();
      p.catchAll = catchAll;
      return this;
    }

    /**
     * Builds the params.
     *
     * @return the params
     */
    public CreateMailboxParams build() {
      check();
      built = true;
      return p;
    }

    private void check() {
      if (built) {
        throw new IllegalStateException("this builder already built its CreateMailboxParams");
      }
    }
  }
}
