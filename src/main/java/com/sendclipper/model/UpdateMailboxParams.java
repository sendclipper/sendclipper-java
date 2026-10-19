package com.sendclipper.model;

import java.util.List;

/**
 * Changes the fields you set.
 *
 * <p>Make one with {@link #builder()}.
 */
public final class UpdateMailboxParams {
  private String localPart;
  private String displayName;
  private String color;
  private Boolean catchAll;
  private Boolean archived;
  private Boolean notifyOwner;
  private List<String> embedOrigins;

  private UpdateMailboxParams() {}

  /**
   * Starts a builder.
   *
   * @return a new builder
   */
  public static Builder builder() {
    return new Builder();
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

  public Boolean getArchived() {
    return archived;
  }

  public Boolean getNotifyOwner() {
    return notifyOwner;
  }

  public List<String> getEmbedOrigins() {
    return embedOrigins;
  }

  /** Builds {@link UpdateMailboxParams}. A builder builds once. */
  public static final class Builder {
    private final UpdateMailboxParams p = new UpdateMailboxParams();
    private boolean built;

    private Builder() {}

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
    public Builder archived(Boolean archived) {
      check();
      p.archived = archived;
      return this;
    }
    public Builder notifyOwner(Boolean notifyOwner) {
      check();
      p.notifyOwner = notifyOwner;
      return this;
    }
    public Builder embedOrigins(List<String> embedOrigins) {
      check();
      p.embedOrigins = embedOrigins;
      return this;
    }

    /**
     * Builds the params.
     *
     * @return the params
     */
    public UpdateMailboxParams build() {
      check();
      built = true;
      return p;
    }

    private void check() {
      if (built) {
        throw new IllegalStateException("this builder already built its UpdateMailboxParams");
      }
    }
  }
}
