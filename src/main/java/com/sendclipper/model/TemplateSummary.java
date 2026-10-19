package com.sendclipper.model;

import java.time.Instant;
import java.util.List;

/** One card in the template list. */
public class TemplateSummary extends ApiObject {
  private String id;
  private String publicId;
  private String name;
  private String status;
  private int blockCount;
  private Instant updatedAt;
  private Instant publishedAt;
  private Instant createdAt;
  private List<NamedRef> domains;
  private List<TemplateTag> tags;
  private String key;
  private NamedRef project;

  public String getId() {
    return id;
  }

  public String getPublicId() {
    return publicId;
  }

  public String getName() {
    return name;
  }

  /**
   * {@code draft} or {@code published}.
   *
   * @return the status
   */
  public String getStatus() {
    return status;
  }

  public int getBlockCount() {
    return blockCount;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public Instant getPublishedAt() {
    return publishedAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public List<NamedRef> getDomains() {
    return domains;
  }

  public List<TemplateTag> getTags() {
    return tags;
  }

  public String getKey() {
    return key;
  }

  public NamedRef getProject() {
    return project;
  }

}
