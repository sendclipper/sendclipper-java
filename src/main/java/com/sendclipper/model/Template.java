package com.sendclipper.model;

import java.util.List;
import java.util.Map;

/** A template with its body. */
public class Template extends TemplateSummary {
  private String subject;
  private String preheader;
  private List<Map<String, Object>> blocks;
  private String html;

  public String getSubject() {
    return subject;
  }

  public String getPreheader() {
    return preheader;
  }

  public List<Map<String, Object>> getBlocks() {
    return blocks;
  }

  public String getHtml() {
    return html;
  }

}
