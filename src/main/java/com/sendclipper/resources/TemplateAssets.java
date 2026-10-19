package com.sendclipper.resources;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sendclipper.RequestOptions;
import com.sendclipper.core.ApiRequest;
import com.sendclipper.core.HttpCore;
import com.sendclipper.model.TemplateAsset;
import com.sendclipper.model.UploadTemplateAssetParams;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Manages images templates can show. */
public final class TemplateAssets {
  private static final TypeReference<List<TemplateAsset>> LIST =
      new TypeReference<List<TemplateAsset>>() {};

  private final HttpCore core;

  TemplateAssets(HttpCore core) {
    this.core = core;
  }

  /**
   * The uploaded images.
   *
   * @return the images
   */
  public List<TemplateAsset> list() {
    return list(null);
  }

  /**
   * The uploaded images, with per-call options.
   *
   * @param opts per-call options
   * @return the images
   */
  public List<TemplateAsset> list(RequestOptions opts) {
    return core.json(ApiRequest.get("/templates/assets"), opts, LIST);
  }

  /**
   * Stores an image and returns its public URL.
   *
   * @param params the image
   * @return the stored image
   */
  public TemplateAsset upload(UploadTemplateAssetParams params) {
    return upload(params, null);
  }

  /**
   * Stores an image, with per-call options.
   *
   * @param params the image
   * @param opts per-call options
   * @return the stored image
   */
  public TemplateAsset upload(UploadTemplateAssetParams params, RequestOptions opts) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("mimeType", params.getMimeType());
    body.put("base64", Base64.getEncoder().encodeToString(params.getData()));
    if (params.getName() != null && !params.getName().isEmpty()) {
      body.put("name", params.getName());
    }
    return core.json(ApiRequest.post("/templates/assets").body(body), opts, TemplateAsset.class);
  }
}
