package com.sendclipper.model;

import java.util.Map;

/**
 * A CSV or vCard import.
 *
 * <p>Make one with {@link #builder()}.
 */
public final class ImportContactsParams {
  private byte[] file;
  private String filename;
  private String audienceId;
  private String newAudience;
  private Boolean createAudiences;
  private String onDuplicate;
  private Map<String, Object> mapping;

  private ImportContactsParams() {}

  /**
   * Starts a builder.
   *
   * @return a new builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * The CSV or vCard contents.
   *
   * @return the file
   */
  public byte[] getFile() {
    return file;
  }

  /**
   * {@code contacts.csv} (the default) or {@code contacts.vcf}: the extension decides the parser.
   *
   * @return the filename
   */
  public String getFilename() {
    return filename;
  }

  public String getAudienceId() {
    return audienceId;
  }

  public String getNewAudience() {
    return newAudience;
  }

  public Boolean getCreateAudiences() {
    return createAudiences;
  }

  /**
   * {@code skip} or {@code update}.
   *
   * @return the on duplicate
   */
  public String getOnDuplicate() {
    return onDuplicate;
  }

  /**
   * The column mapping as {@code importPreview} returns it.
   *
   * @return the mapping
   */
  public Map<String, Object> getMapping() {
    return mapping;
  }

  /** Builds {@link ImportContactsParams}. A builder builds once. */
  public static final class Builder {
    private final ImportContactsParams p = new ImportContactsParams();
    private boolean built;

    private Builder() {}

    /**
     * The CSV or vCard contents.
     *
     * @param file the file
     * @return this builder
     */
    public Builder file(byte[] file) {
      check();
      p.file = file;
      return this;
    }
    /**
     * {@code contacts.csv} (the default) or {@code contacts.vcf}: the extension decides the parser.
     *
     * @param filename the filename
     * @return this builder
     */
    public Builder filename(String filename) {
      check();
      p.filename = filename;
      return this;
    }
    public Builder audienceId(String audienceId) {
      check();
      p.audienceId = audienceId;
      return this;
    }
    public Builder newAudience(String newAudience) {
      check();
      p.newAudience = newAudience;
      return this;
    }
    public Builder createAudiences(Boolean createAudiences) {
      check();
      p.createAudiences = createAudiences;
      return this;
    }
    /**
     * {@code skip} or {@code update}.
     *
     * @param onDuplicate the on duplicate
     * @return this builder
     */
    public Builder onDuplicate(String onDuplicate) {
      check();
      p.onDuplicate = onDuplicate;
      return this;
    }
    /**
     * The column mapping as {@code importPreview} returns it.
     *
     * @param mapping the mapping
     * @return this builder
     */
    public Builder mapping(Map<String, Object> mapping) {
      check();
      p.mapping = mapping;
      return this;
    }

    /**
     * Builds the params.
     *
     * @return the params
     */
    public ImportContactsParams build() {
      check();
      built = true;
      return p;
    }

    private void check() {
      if (built) {
        throw new IllegalStateException("this builder already built its ImportContactsParams");
      }
    }
  }
}
