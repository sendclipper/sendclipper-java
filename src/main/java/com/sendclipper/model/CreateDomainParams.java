package com.sendclipper.model;

/**
 * Registers a domain.
 *
 * <p>Make one with {@link #builder()}.
 */
public final class CreateDomainParams {
  private String name;
  private Region region;

  private CreateDomainParams() {}

  /**
   * Starts a builder.
   *
   * @return a new builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * A hostname such as {@code send.acme.com}. No scheme, no trailing slash.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Cannot change once the domain holds messages.
   *
   * @return the region
   */
  public Region getRegion() {
    return region;
  }

  /** Builds {@link CreateDomainParams}. A builder builds once. */
  public static final class Builder {
    private final CreateDomainParams p = new CreateDomainParams();
    private boolean built;

    private Builder() {}

    /**
     * A hostname such as {@code send.acme.com}. No scheme, no trailing slash.
     *
     * @param name the name
     * @return this builder
     */
    public Builder name(String name) {
      check();
      p.name = name;
      return this;
    }
    /**
     * Cannot change once the domain holds messages.
     *
     * @param region the region
     * @return this builder
     */
    public Builder region(Region region) {
      check();
      p.region = region;
      return this;
    }

    /**
     * Builds the params.
     *
     * @return the params
     */
    public CreateDomainParams build() {
      check();
      built = true;
      return p;
    }

    private void check() {
      if (built) {
        throw new IllegalStateException("this builder already built its CreateDomainParams");
      }
    }
  }
}
