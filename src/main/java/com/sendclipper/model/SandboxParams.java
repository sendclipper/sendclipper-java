package com.sendclipper.model;

/**
 * Creates or renames a sandbox.
 *
 * <p>Make one with {@link #builder()}.
 */
public final class SandboxParams {
  private String name;
  private String projectLabel;
  private String color;

  private SandboxParams() {}

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

  public String getProjectLabel() {
    return projectLabel;
  }

  /**
   * {@code #rrggbb}.
   *
   * @return the color
   */
  public String getColor() {
    return color;
  }

  /** Builds {@link SandboxParams}. A builder builds once. */
  public static final class Builder {
    private final SandboxParams p = new SandboxParams();
    private boolean built;

    private Builder() {}

    public Builder name(String name) {
      check();
      p.name = name;
      return this;
    }
    public Builder projectLabel(String projectLabel) {
      check();
      p.projectLabel = projectLabel;
      return this;
    }
    /**
     * {@code #rrggbb}.
     *
     * @param color the color
     * @return this builder
     */
    public Builder color(String color) {
      check();
      p.color = color;
      return this;
    }

    /**
     * Builds the params.
     *
     * @return the params
     */
    public SandboxParams build() {
      check();
      built = true;
      return p;
    }

    private void check() {
      if (built) {
        throw new IllegalStateException("this builder already built its SandboxParams");
      }
    }
  }
}
