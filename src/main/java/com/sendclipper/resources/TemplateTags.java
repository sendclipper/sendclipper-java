package com.sendclipper.resources;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sendclipper.RequestOptions;
import com.sendclipper.core.ApiRequest;
import com.sendclipper.core.HttpCore;
import com.sendclipper.model.TemplateTag;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Manages template tags. */
public final class TemplateTags {
  private static final TypeReference<List<TemplateTag>> LIST =
      new TypeReference<List<TemplateTag>>() {};

  private static final class Removed {
    int templateCount;
  }

  private final HttpCore core;

  TemplateTags(HttpCore core) {
    this.core = core;
  }

  /**
   * Every tag, each with its template count.
   *
   * @return the tags
   */
  public List<TemplateTag> list() {
    return list(null);
  }

  /**
   * Every tag, with per-call options.
   *
   * @param opts per-call options
   * @return the tags
   */
  public List<TemplateTag> list(RequestOptions opts) {
    return core.json(ApiRequest.get("/templates/tags"), opts, LIST);
  }

  /**
   * Makes a tag.
   *
   * @param name the tag's name
   * @param color gray, red, orange, amber, green, teal, blue, purple or pink
   * @return the tag
   */
  public TemplateTag create(String name, String color) {
    return create(name, color, null);
  }

  /**
   * Makes a tag, with per-call options.
   *
   * @param name the tag's name
   * @param color its colour
   * @param opts per-call options
   * @return the tag
   */
  public TemplateTag create(String name, String color, RequestOptions opts) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("name", name);
    body.put("color", color);
    return core.json(ApiRequest.post("/templates/tags").body(body), opts, TemplateTag.class);
  }

  /**
   * Renames or recolours a tag; a null or empty value leaves that field alone.
   *
   * @param id the tag's id
   * @param name the new name, or null
   * @param color the new colour, or null
   * @return the tag
   */
  public TemplateTag update(String id, String name, String color) {
    return update(id, name, color, null);
  }

  /**
   * Changes a tag, with per-call options.
   *
   * @param id the tag's id
   * @param name the new name, or null
   * @param color the new colour, or null
   * @param opts per-call options
   * @return the tag
   */
  public TemplateTag update(String id, String name, String color, RequestOptions opts) {
    Map<String, Object> body = new LinkedHashMap<>();
    if (name != null && !name.isEmpty()) {
      body.put("name", name);
    }
    if (color != null && !color.isEmpty()) {
      body.put("color", color);
    }
    return core.json(
        ApiRequest.patch("/templates/tags/" + ApiRequest.seg(id)).body(body),
        opts,
        TemplateTag.class);
  }

  /**
   * Deletes a tag.
   *
   * @param id the tag's id
   * @return how many templates it was taken off
   */
  public int remove(String id) {
    return remove(id, null);
  }

  /**
   * Deletes a tag, with per-call options.
   *
   * @param id the tag's id
   * @param opts per-call options
   * @return how many templates it was taken off
   */
  public int remove(String id, RequestOptions opts) {
    Removed r =
        core.json(ApiRequest.delete("/templates/tags/" + ApiRequest.seg(id)), opts, Removed.class);
    return r == null ? 0 : r.templateCount;
  }
}
