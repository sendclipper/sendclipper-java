package com.sendclipper.model;

import java.util.List;
import java.util.Map;

/**
 * Changes the fields you set. Set one to {@code ""} (or an empty list) to clear it.
 *
 * <p>Make one with {@link #builder()}.
 */
public final class UpdateTemplateParams {
  private String name;
  private String subject;
  private String preheader;
  private List<Map<String, Object>> blocks;
  private List<String> domainIds;
  private String projectDomainId;
  private String key;

  private UpdateTemplateParams() {}

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

  public String getSubject() {
    return subject;
  }

  public String getPreheader() {
    return preheader;
  }

  public List<Map<String, Object>> getBlocks() {
    return blocks;
  }

  public List<String> getDomainIds() {
    return domainIds;
  }

  public String getProjectDomainId() {
    return projectDomainId;
  }

  public String getKey() {
    return key;
  }

  /** Builds {@link UpdateTemplateParams}. A builder builds once. */
  public static final class Builder {
    private final UpdateTemplateParams p = new UpdateTemplateParams();
    private boolean built;

    private Builder() {}

    public Builder name(String name) {
      check();
      p.name = name;
      return this;
    }
    public Builder subject(String subject) {
      check();
      p.subject = subject;
      return this;
    }
    public Builder preheader(String preheader) {
      check();
      p.preheader = preheader;
      return this;
    }
    public Builder blocks(List<Map<String, Object>> blocks) {
      check();
      p.blocks = blocks;
      return this;
    }
    public Builder domainIds(List<String> domainIds) {
      check();
      p.domainIds = domainIds;
      return this;
    }
    public Builder projectDomainId(String projectDomainId) {
      check();
      p.projectDomainId = projectDomainId;
      return this;
    }
    public Builder key(String key) {
      check();
      p.key = key;
      return this;
    }

    /**
     * Adds one block: {@code Map.of("type", "text", "text", "Hi {{first_name|there}}")}.
     *
     * @param block the block
     * @return this builder
     */
    public Builder block(Map<String, Object> block) {
      check();
      if (p.blocks == null) {
        p.blocks = new java.util.ArrayList<>();
      }
      p.blocks.add(block);
      return this;
    }

    /**
     * Builds the params.
     *
     * @return the params
     */
    public UpdateTemplateParams build() {
      check();
      built = true;
      return p;
    }

    private void check() {
      if (built) {
        throw new IllegalStateException("this builder already built its UpdateTemplateParams");
      }
    }
  }
}
