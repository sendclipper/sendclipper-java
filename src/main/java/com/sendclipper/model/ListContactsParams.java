package com.sendclipper.model;

/**
 * Filters contacts.
 *
 * <p>Make one with {@link #builder()}.
 */
public final class ListContactsParams {
  private String audienceId;
  private String status;
  private Integer page;
  private Integer pageSize;

  private ListContactsParams() {}

  /**
   * Starts a builder.
   *
   * @return a new builder
   */
  public static Builder builder() {
    return new Builder();
  }

  public String getAudienceId() {
    return audienceId;
  }

  public String getStatus() {
    return status;
  }

  public Integer getPage() {
    return page;
  }

  /**
   * 1-200, default 25.
   *
   * @return the page size
   */
  public Integer getPageSize() {
    return pageSize;
  }

  /** Builds {@link ListContactsParams}. A builder builds once. */
  public static final class Builder {
    private final ListContactsParams p = new ListContactsParams();
    private boolean built;

    private Builder() {}

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
    public Builder page(Integer page) {
      check();
      p.page = page;
      return this;
    }
    /**
     * 1-200, default 25.
     *
     * @param pageSize the page size
     * @return this builder
     */
    public Builder pageSize(Integer pageSize) {
      check();
      p.pageSize = pageSize;
      return this;
    }

    /**
     * Builds the params.
     *
     * @return the params
     */
    public ListContactsParams build() {
      check();
      built = true;
      return p;
    }

    private void check() {
      if (built) {
        throw new IllegalStateException("this builder already built its ListContactsParams");
      }
    }
  }
}
