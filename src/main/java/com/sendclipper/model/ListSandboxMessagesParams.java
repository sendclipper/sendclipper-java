package com.sendclipper.model;

/**
 * Filters a sandbox's messages.
 *
 * <p>Make one with {@link #builder()}.
 */
public final class ListSandboxMessagesParams {
  private String search;
  private Integer limit;

  private ListSandboxMessagesParams() {}

  /**
   * Starts a builder.
   *
   * @return a new builder
   */
  public static Builder builder() {
    return new Builder();
  }

  public String getSearch() {
    return search;
  }

  public Integer getLimit() {
    return limit;
  }

  /** Builds {@link ListSandboxMessagesParams}. A builder builds once. */
  public static final class Builder {
    private final ListSandboxMessagesParams p = new ListSandboxMessagesParams();
    private boolean built;

    private Builder() {}

    public Builder search(String search) {
      check();
      p.search = search;
      return this;
    }
    public Builder limit(Integer limit) {
      check();
      p.limit = limit;
      return this;
    }

    /**
     * Builds the params.
     *
     * @return the params
     */
    public ListSandboxMessagesParams build() {
      check();
      built = true;
      return p;
    }

    private void check() {
      if (built) {
        throw new IllegalStateException("this builder already built its ListSandboxMessagesParams");
      }
    }
  }
}
