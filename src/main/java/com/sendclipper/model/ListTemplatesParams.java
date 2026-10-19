package com.sendclipper.model;

/**
 * Filters the template list.
 *
 * <p>Make one with {@link #builder()}.
 */
public final class ListTemplatesParams {
  private String tag;
  private String project;

  private ListTemplatesParams() {}

  /**
   * Starts a builder.
   *
   * @return a new builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * A tag id.
   *
   * @return the tag
   */
  public String getTag() {
    return tag;
  }

  /**
   * A project (domain) id.
   *
   * @return the project
   */
  public String getProject() {
    return project;
  }

  /** Builds {@link ListTemplatesParams}. A builder builds once. */
  public static final class Builder {
    private final ListTemplatesParams p = new ListTemplatesParams();
    private boolean built;

    private Builder() {}

    /**
     * A tag id.
     *
     * @param tag the tag
     * @return this builder
     */
    public Builder tag(String tag) {
      check();
      p.tag = tag;
      return this;
    }
    /**
     * A project (domain) id.
     *
     * @param project the project
     * @return this builder
     */
    public Builder project(String project) {
      check();
      p.project = project;
      return this;
    }

    /**
     * Builds the params.
     *
     * @return the params
     */
    public ListTemplatesParams build() {
      check();
      built = true;
      return p;
    }

    private void check() {
      if (built) {
        throw new IllegalStateException("this builder already built its ListTemplatesParams");
      }
    }
  }
}
