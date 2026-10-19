package com.sendclipper.model;

/**
 * One image.
 *
 * <p>Make one with {@link #builder()}.
 */
public final class UploadTemplateAssetParams {
  private String mimeType;
  private byte[] data;
  private String name;

  private UploadTemplateAssetParams() {}

  /**
   * Starts a builder.
   *
   * @return a new builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * {@code image/png}, {@code image/jpeg}, {@code image/gif} or {@code image/webp}.
   *
   * @return the mime type
   */
  public String getMimeType() {
    return mimeType;
  }

  /**
   * The file's bytes; they are base64-encoded for you.
   *
   * @return the data
   */
  public byte[] getData() {
    return data;
  }

  public String getName() {
    return name;
  }

  /** Builds {@link UploadTemplateAssetParams}. A builder builds once. */
  public static final class Builder {
    private final UploadTemplateAssetParams p = new UploadTemplateAssetParams();
    private boolean built;

    private Builder() {}

    /**
     * {@code image/png}, {@code image/jpeg}, {@code image/gif} or {@code image/webp}.
     *
     * @param mimeType the mime type
     * @return this builder
     */
    public Builder mimeType(String mimeType) {
      check();
      p.mimeType = mimeType;
      return this;
    }
    /**
     * The file's bytes; they are base64-encoded for you.
     *
     * @param data the data
     * @return this builder
     */
    public Builder data(byte[] data) {
      check();
      p.data = data;
      return this;
    }
    public Builder name(String name) {
      check();
      p.name = name;
      return this;
    }

    /**
     * Builds the params.
     *
     * @return the params
     */
    public UploadTemplateAssetParams build() {
      check();
      built = true;
      return p;
    }

    private void check() {
      if (built) {
        throw new IllegalStateException("this builder already built its UploadTemplateAssetParams");
      }
    }
  }
}
