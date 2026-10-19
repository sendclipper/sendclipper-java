package com.sendclipper.model;

/**
 * Filters the list.
 *
 * <p>Make one with {@link #builder()}.
 */
public final class ListSuppressionsParams {
  private String q;
  private String reason;
  private String cursor;
  private Integer limit;

  private ListSuppressionsParams() {}

  /**
   * Starts a builder.
   *
   * @return a new builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * An address search.
   *
   * @return the q
   */
  public String getQ() {
    return q;
  }

  public String getReason() {
    return reason;
  }

  public String getCursor() {
    return cursor;
  }

  /**
   * 1-100.
   *
   * @return the limit
   */
  public Integer getLimit() {
    return limit;
  }

  /** Builds {@link ListSuppressionsParams}. A builder builds once. */
  public static final class Builder {
    private final ListSuppressionsParams p = new ListSuppressionsParams();
    private boolean built;

    private Builder() {}

    /**
     * An address search.
     *
     * @param q the q
     * @return this builder
     */
    public Builder q(String q) {
      check();
      p.q = q;
      return this;
    }
    public Builder reason(String reason) {
      check();
      p.reason = reason;
      return this;
    }
    public Builder cursor(String cursor) {
      check();
      p.cursor = cursor;
      return this;
    }
    /**
     * 1-100.
     *
     * @param limit the limit
     * @return this builder
     */
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
    public ListSuppressionsParams build() {
      check();
      built = true;
      return p;
    }

    private void check() {
      if (built) {
        throw new IllegalStateException("this builder already built its ListSuppressionsParams");
      }
    }
  }
}
