package com.sendclipper.model;

/**
 * Picks the export format.
 *
 * <p>Make one with {@link #builder()}.
 */
public final class ExportContactsParams {
  private String format;
  private String audience;

  private ExportContactsParams() {}

  /**
   * Starts a builder.
   *
   * @return a new builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * {@code csv} (the default) or {@code vcf}.
   *
   * @return the format
   */
  public String getFormat() {
    return format;
  }

  public String getAudience() {
    return audience;
  }

  /** Builds {@link ExportContactsParams}. A builder builds once. */
  public static final class Builder {
    private final ExportContactsParams p = new ExportContactsParams();
    private boolean built;

    private Builder() {}

    /**
     * {@code csv} (the default) or {@code vcf}.
     *
     * @param format the format
     * @return this builder
     */
    public Builder format(String format) {
      check();
      p.format = format;
      return this;
    }
    public Builder audience(String audience) {
      check();
      p.audience = audience;
      return this;
    }

    /**
     * Builds the params.
     *
     * @return the params
     */
    public ExportContactsParams build() {
      check();
      built = true;
      return p;
    }

    private void check() {
      if (built) {
        throw new IllegalStateException("this builder already built its ExportContactsParams");
      }
    }
  }
}
