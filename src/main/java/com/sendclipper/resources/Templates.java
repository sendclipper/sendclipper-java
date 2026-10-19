package com.sendclipper.resources;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sendclipper.RequestOptions;
import com.sendclipper.core.ApiRequest;
import com.sendclipper.core.HttpCore;
import com.sendclipper.model.CreateTemplateParams;
import com.sendclipper.model.ListTemplatesParams;
import com.sendclipper.model.NamedRef;
import com.sendclipper.model.Template;
import com.sendclipper.model.TemplatePreview;
import com.sendclipper.model.TemplateSummary;
import com.sendclipper.model.TemplateTag;
import com.sendclipper.model.UpdateTemplateParams;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Manages email templates. */
public final class Templates {
  private static final TypeReference<List<TemplateSummary>> LIST =
      new TypeReference<List<TemplateSummary>>() {};
  private static final TypeReference<List<NamedRef>> PROJECTS =
      new TypeReference<List<NamedRef>>() {};

  private static final class TagsAnswer {
    List<TemplateTag> tags;
  }

  private final HttpCore core;
  private final TemplateTags tags;
  private final TemplateAssets assets;

  /**
   * The SDK makes these; use {@code client.templates()}.
   *
   * @param core the engine
   */
  public Templates(HttpCore core) {
    this.core = core;
    this.tags = new TemplateTags(core);
    this.assets = new TemplateAssets(core);
  }

  /**
   * Template tags.
   *
   * @return the tags resource
   */
  public TemplateTags tags() {
    return tags;
  }

  /**
   * Images templates can show.
   *
   * @return the assets resource
   */
  public TemplateAssets assets() {
    return assets;
  }

  /**
   * The templates, optionally filtered.
   *
   * @param params filters, or null
   * @return the templates
   */
  public List<TemplateSummary> list(ListTemplatesParams params) {
    return list(params, null);
  }

  /**
   * The templates, with per-call options.
   *
   * @param params filters, or null
   * @param opts per-call options
   * @return the templates
   */
  public List<TemplateSummary> list(ListTemplatesParams params, RequestOptions opts) {
    ApiRequest req = ApiRequest.get("/templates");
    if (params != null) {
      req.query("tag", params.getTag()).query("project", params.getProject());
    }
    return core.json(req, opts, LIST);
  }

  /**
   * A template by uuid or public id ({@code tpl_...}).
   *
   * @param id the template's id
   * @return the template
   */
  public Template get(String id) {
    return get(id, null);
  }

  /**
   * A template, with per-call options.
   *
   * @param id the template's id
   * @param opts per-call options
   * @return the template
   */
  public Template get(String id, RequestOptions opts) {
    return core.json(ApiRequest.get(path(id)), opts, Template.class);
  }

  /**
   * The rendered HTML, tags left in place.
   *
   * @param id the template's id
   * @return the HTML
   */
  public String html(String id) {
    return html(id, null);
  }

  /**
   * The rendered HTML, with per-call options.
   *
   * @param id the template's id
   * @param opts per-call options
   * @return the HTML
   */
  public String html(String id, RequestOptions opts) {
    return core.text(ApiRequest.get(path(id) + "/html"), opts);
  }

  /**
   * Makes a new draft. Publish it before sending with it.
   *
   * @param params the draft, or null for an empty one
   * @return the template
   */
  public Template create(CreateTemplateParams params) {
    return create(params, null);
  }

  /**
   * Makes a draft, with per-call options.
   *
   * @param params the draft, or null for an empty one
   * @param opts per-call options
   * @return the template
   */
  public Template create(CreateTemplateParams params, RequestOptions opts) {
    Object body = params == null ? Collections.emptyMap() : params;
    return core.json(ApiRequest.post("/templates").body(body), opts, Template.class);
  }

  /**
   * Changes the fields set in params.
   *
   * @param id the template's id
   * @param params the changes
   * @return the template
   */
  public Template update(String id, UpdateTemplateParams params) {
    return update(id, params, null);
  }

  /**
   * Changes a template, with per-call options.
   *
   * @param id the template's id
   * @param params the changes
   * @param opts per-call options
   * @return the template
   */
  public Template update(String id, UpdateTemplateParams params, RequestOptions opts) {
    return core.json(ApiRequest.patch(path(id)).body(params), opts, Template.class);
  }

  /**
   * Deletes a template.
   *
   * @param id the template's id
   */
  public void remove(String id) {
    remove(id, null);
  }

  /**
   * Deletes a template, with per-call options.
   *
   * @param id the template's id
   * @param opts per-call options
   */
  public void remove(String id, RequestOptions opts) {
    core.discard(ApiRequest.delete(path(id)), opts);
  }

  /**
   * Copies a template as a new draft.
   *
   * @param id the template's id
   * @return the copy
   */
  public Template duplicate(String id) {
    return action(id, "duplicate", null);
  }

  /**
   * Copies a template, with per-call options.
   *
   * @param id the template's id
   * @param opts per-call options
   * @return the copy
   */
  public Template duplicate(String id, RequestOptions opts) {
    return action(id, "duplicate", opts);
  }

  /**
   * Makes a template sendable.
   *
   * @param id the template's id
   * @return the template
   */
  public Template publish(String id) {
    return action(id, "publish", null);
  }

  /**
   * Publishes, with per-call options.
   *
   * @param id the template's id
   * @param opts per-call options
   * @return the template
   */
  public Template publish(String id, RequestOptions opts) {
    return action(id, "publish", opts);
  }

  /**
   * Returns a template to draft.
   *
   * @param id the template's id
   * @return the template
   */
  public Template unpublish(String id) {
    return action(id, "unpublish", null);
  }

  /**
   * Unpublishes, with per-call options.
   *
   * @param id the template's id
   * @param opts per-call options
   * @return the template
   */
  public Template unpublish(String id, RequestOptions opts) {
    return action(id, "unpublish", opts);
  }

  /**
   * Renders with variables without sending anything.
   *
   * @param id the template's id
   * @param variables tag values, or null
   * @return the render
   */
  public TemplatePreview preview(String id, Map<String, ?> variables) {
    return preview(id, variables, null);
  }

  /**
   * Renders, with per-call options.
   *
   * @param id the template's id
   * @param variables tag values, or null
   * @param opts per-call options
   * @return the render
   */
  public TemplatePreview preview(String id, Map<String, ?> variables, RequestOptions opts) {
    Map<String, ?> vars = variables == null ? Collections.emptyMap() : variables;
    return core.json(
        ApiRequest.post(path(id) + "/preview").body(Collections.singletonMap("variables", vars)),
        opts,
        TemplatePreview.class);
  }

  /**
   * Replaces the template's tags with these tag ids.
   *
   * @param id the template's id
   * @param tagIds the tag ids (empty clears them)
   * @return the template's tags
   */
  public List<TemplateTag> setTags(String id, List<String> tagIds) {
    return setTags(id, tagIds, null);
  }

  /**
   * Replaces the tags, with per-call options.
   *
   * @param id the template's id
   * @param tagIds the tag ids
   * @param opts per-call options
   * @return the template's tags
   */
  public List<TemplateTag> setTags(String id, List<String> tagIds, RequestOptions opts) {
    List<String> ids = tagIds == null ? Collections.emptyList() : tagIds;
    TagsAnswer res =
        core.json(
            ApiRequest.put(path(id) + "/tags").body(Collections.singletonMap("tagIds", ids)),
            opts,
            TagsAnswer.class);
    return res == null || res.tags == null ? Collections.emptyList() : res.tags;
  }

  /**
   * The projects (domains) templates can belong to.
   *
   * @return the projects
   */
  public List<NamedRef> projects() {
    return projects(null);
  }

  /**
   * The projects, with per-call options.
   *
   * @param opts per-call options
   * @return the projects
   */
  public List<NamedRef> projects(RequestOptions opts) {
    return core.json(ApiRequest.get("/templates/projects"), opts, PROJECTS);
  }

  private Template action(String id, String verb, RequestOptions opts) {
    return core.json(ApiRequest.post(path(id) + "/" + verb), opts, Template.class);
  }

  private static String path(String id) {
    return "/templates/" + ApiRequest.seg(id);
  }
}
