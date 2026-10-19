package com.sendclipper.model;

/**
 * Filters the sent-mail log.
 *
 * <p>Make one with {@link #builder()}.
 */
public final class ListEmailsParams {
  private String search;
  private EmailStatus status;
  private String domain;
  private String broadcastId;
  private String from;
  private String to;
  private Integer page;
  private Integer pageSize;

  private ListEmailsParams() {}

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

  public EmailStatus getStatus() {
    return status;
  }

  /**
   * The exact sending domain.
   *
   * @return the domain
   */
  public String getDomain() {
    return domain;
  }

  public String getBroadcastId() {
    return broadcastId;
  }

  /**
   * An ISO 8601 lower bound.
   *
   * @return the from
   */
  public String getFrom() {
    return from;
  }

  /**
   * An ISO 8601 upper bound.
   *
   * @return the to
   */
  public String getTo() {
    return to;
  }

  public Integer getPage() {
    return page;
  }

  /**
   * 1-100, default 50.
   *
   * @return the page size
   */
  public Integer getPageSize() {
    return pageSize;
  }

  /** Builds {@link ListEmailsParams}. A builder builds once. */
  public static final class Builder {
    private final ListEmailsParams p = new ListEmailsParams();
    private boolean built;

    private Builder() {}

    public Builder search(String search) {
      check();
      p.search = search;
      return this;
    }
    public Builder status(EmailStatus status) {
      check();
      p.status = status;
      return this;
    }
    /**
     * The exact sending domain.
     *
     * @param domain the domain
     * @return this builder
     */
    public Builder domain(String domain) {
      check();
      p.domain = domain;
      return this;
    }
    public Builder broadcastId(String broadcastId) {
      check();
      p.broadcastId = broadcastId;
      return this;
    }
    /**
     * An ISO 8601 lower bound.
     *
     * @param from the from
     * @return this builder
     */
    public Builder from(String from) {
      check();
      p.from = from;
      return this;
    }
    /**
     * An ISO 8601 upper bound.
     *
     * @param to the to
     * @return this builder
     */
    public Builder to(String to) {
      check();
      p.to = to;
      return this;
    }
    public Builder page(Integer page) {
      check();
      p.page = page;
      return this;
    }
    /**
     * 1-100, default 50.
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
    public ListEmailsParams build() {
      check();
      built = true;
      return p;
    }

    private void check() {
      if (built) {
        throw new IllegalStateException("this builder already built its ListEmailsParams");
      }
    }
  }
}
