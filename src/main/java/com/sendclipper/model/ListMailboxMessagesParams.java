package com.sendclipper.model;

/**
 * Filters an address's mail.
 *
 * <p>Make one with {@link #builder()}.
 */
public final class ListMailboxMessagesParams {
  private String folder;
  private String search;
  private String label;
  private Integer limit;
  private String cursor;

  private ListMailboxMessagesParams() {}

  /**
   * Starts a builder.
   *
   * @return a new builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * {@code all}, {@code inbox}, {@code starred}, {@code sent}, {@code drafts}, {@code archive}, {@code spam} or {@code trash}.
   *
   * @return the folder
   */
  public String getFolder() {
    return folder;
  }

  public String getSearch() {
    return search;
  }

  public String getLabel() {
    return label;
  }

  /**
   * 1-200.
   *
   * @return the limit
   */
  public Integer getLimit() {
    return limit;
  }

  public String getCursor() {
    return cursor;
  }

  /** Builds {@link ListMailboxMessagesParams}. A builder builds once. */
  public static final class Builder {
    private final ListMailboxMessagesParams p = new ListMailboxMessagesParams();
    private boolean built;

    private Builder() {}

    /**
     * {@code all}, {@code inbox}, {@code starred}, {@code sent}, {@code drafts}, {@code archive}, {@code spam} or {@code trash}.
     *
     * @param folder the folder
     * @return this builder
     */
    public Builder folder(String folder) {
      check();
      p.folder = folder;
      return this;
    }
    public Builder search(String search) {
      check();
      p.search = search;
      return this;
    }
    public Builder label(String label) {
      check();
      p.label = label;
      return this;
    }
    /**
     * 1-200.
     *
     * @param limit the limit
     * @return this builder
     */
    public Builder limit(Integer limit) {
      check();
      p.limit = limit;
      return this;
    }
    public Builder cursor(String cursor) {
      check();
      p.cursor = cursor;
      return this;
    }

    /**
     * Builds the params.
     *
     * @return the params
     */
    public ListMailboxMessagesParams build() {
      check();
      built = true;
      return p;
    }

    private void check() {
      if (built) {
        throw new IllegalStateException("this builder already built its ListMailboxMessagesParams");
      }
    }
  }
}
