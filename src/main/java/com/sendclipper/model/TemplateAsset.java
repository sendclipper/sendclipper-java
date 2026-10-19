package com.sendclipper.model;

import java.time.Instant;

/** An uploaded image. */
public class TemplateAsset extends ApiObject {
  private String id;
  private String publicId;
  private String url;
  private String mimeType;
  private long bytes;
  private String name;
  private Instant createdAt;

  public String getId() {
    return id;
  }

  public String getPublicId() {
    return publicId;
  }

  /**
   * Public: a mail client can load it.
   *
   * @return the url
   */
  public String getUrl() {
    return url;
  }

  public String getMimeType() {
    return mimeType;
  }

  public long getBytes() {
    return bytes;
  }

  public String getName() {
    return name;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

}
