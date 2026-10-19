package com.sendclipper.model;

import java.util.List;

/** A render that sent nothing. */
public class TemplatePreview extends ApiObject {
  private String subject;
  private String preheader;
  private String html;
  private String text;
  private List<Variable> variables;
  private List<String> unsafeLinks;

  public String getSubject() {
    return subject;
  }

  public String getPreheader() {
    return preheader;
  }

  public String getHtml() {
    return html;
  }

  public String getText() {
    return text;
  }

  public List<Variable> getVariables() {
    return variables;
  }

  /**
   * Links whose target a tag would make unsafe.
   *
   * @return the unsafe links
   */
  public List<String> getUnsafeLinks() {
    return unsafeLinks;
  }

  /** One tag and what it rendered as. */
  public static class Variable extends ApiObject {
    private String name;
    private String fallback;
    private String value;
    private boolean provided;

    public String getName() {
      return name;
    }

    public String getFallback() {
      return fallback;
    }

    public String getValue() {
      return value;
    }

    public boolean isProvided() {
      return provided;
    }
  }
}
