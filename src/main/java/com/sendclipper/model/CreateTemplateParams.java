package com.sendclipper.model;

import java.util.List;
import java.util.Map;

/**
 * Makes a draft. Every field is optional.
 *
 * <p>Make one with {@link #builder()}.
 */
public final class CreateTemplateParams {
  private String name;
  private String subject;
  private String preheader;
  private List<Map<String, Object>> blocks;
  private String projectDomainId;
  private String key;

  private CreateTemplateParams() {}

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

  /**
   * See https://sendclipper.com/docs for the block types. Unknown fields are dropped.
   *
   * @return the blocks
   */
  public List<Map<String, Object>> getBlocks() {
    return blocks;
  }

  public String getProjectDomainId() {
    return projectDomainId;
  }

  /**
   * The send name, unique in its project.
   *
   * @return the key
   */
  public String getKey() {
    return key;
  }

  /** Builds {@link CreateTemplateParams}. A builder builds once. */
  public static final class Builder {
    private final CreateTemplateParams p = new CreateTemplateParams();
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
    /**
     * See https://sendclipper.com/docs for the block types. Unknown fields are dropped.
     *
     * @param blocks the blocks
     * @return this builder
     */
    public Builder blocks(List<Map<String, Object>> blocks) {
      check();
      p.blocks = blocks;
      return this;
    }
    public Builder projectDomainId(String projectDomainId) {
      check();
      p.projectDomainId = projectDomainId;
      return this;
    }
    /**
     * The send name, unique in its project.
     *
     * @param key the key
     * @return this builder
     */
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
    public CreateTemplateParams build() {
      check();
      built = true;
      return p;
    }

    private void check() {
      if (built) {
        throw new IllegalStateException("this builder already built its CreateTemplateParams");
      }
    }
  }
}
