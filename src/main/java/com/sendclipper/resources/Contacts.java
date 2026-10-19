package com.sendclipper.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.sendclipper.PageIterable;
import com.sendclipper.RequestOptions;
import com.sendclipper.core.ApiRequest;
import com.sendclipper.core.HttpCore;
import com.sendclipper.core.Json;
import com.sendclipper.core.Multipart;
import com.sendclipper.core.Paging;
import com.sendclipper.model.Contact;
import com.sendclipper.model.ContactList;
import com.sendclipper.model.CreateContactParams;
import com.sendclipper.model.ExportContactsParams;
import com.sendclipper.model.ImportContactsParams;
import com.sendclipper.model.ListContactsParams;
import java.util.List;
import java.util.Locale;

/** Manages contacts. */
public final class Contacts {
  private final HttpCore core;

  /**
   * The SDK makes these; use {@code client.contacts()}.
   *
   * @param core the engine
   */
  public Contacts(HttpCore core) {
    this.core = core;
  }

  /**
   * One page of contacts.
   *
   * @param params filters, or null
   * @return the page
   */
  public ContactList list(ListContactsParams params) {
    return list(params, null);
  }

  /**
   * One page of contacts, with per-call options.
   *
   * @param params filters, or null
   * @param opts per-call options
   * @return the page
   */
  public ContactList list(ListContactsParams params, RequestOptions opts) {
    return list(
        params,
        params == null ? null : params.getPage(),
        params == null ? null : params.getPageSize(),
        opts);
  }

  private ContactList list(ListContactsParams p, Integer page, Integer size, RequestOptions opts) {
    ApiRequest req = ApiRequest.get("/contacts");
    if (p != null) {
      req.query("audienceId", p.getAudienceId()).query("status", p.getStatus());
    }
    req.query("page", page).query("pageSize", size);
    return core.json(req, opts, ContactList.class);
  }

  /**
   * Every contact, page after page.
   *
   * @param params filters, or null; its page is ignored
   * @return the contacts
   */
  public PageIterable<Contact> all(ListContactsParams params) {
    return all(params, null);
  }

  /**
   * Every contact, with per-call options for each page.
   *
   * @param params filters, or null; its page is ignored
   * @param opts per-call options
   * @return the contacts
   */
  public PageIterable<Contact> all(ListContactsParams params, RequestOptions opts) {
    int size = params != null && params.getPageSize() != null ? params.getPageSize() : 200;
    return Paging.numbered(
        page -> {
          ContactList res = list(params, page, size, opts);
          List<Contact> items = res.getItems();
          return new Paging.Numbered<>(items, page >= res.getTotalPages());
        });
  }

  /**
   * Adds a contact.
   *
   * @param params the contact
   * @return the contact
   */
  public Contact create(CreateContactParams params) {
    return create(params, null);
  }

  /**
   * Adds a contact, with per-call options.
   *
   * @param params the contact
   * @param opts per-call options
   * @return the contact
   */
  public Contact create(CreateContactParams params, RequestOptions opts) {
    return core.json(ApiRequest.post("/contacts").body(params), opts, Contact.class);
  }

  /**
   * Imports a CSV or vCard file. Importing never suppresses anyone.
   *
   * @param params the file and how to import it
   * @return what the import did
   */
  public JsonNode importContacts(ImportContactsParams params) {
    return importContacts(params, null);
  }

  /**
   * Imports a file, with per-call options. An upload is never retried.
   *
   * @param params the file and how to import it
   * @param opts per-call options
   * @return what the import did
   */
  public JsonNode importContacts(ImportContactsParams params, RequestOptions opts) {
    return upload("/contacts/import", params, opts);
  }

  /**
   * What an import would do, without importing.
   *
   * @param params the file and how to import it
   * @return the preview, with the column mapping
   */
  public JsonNode importPreview(ImportContactsParams params) {
    return importPreview(params, null);
  }

  /**
   * An import preview, with per-call options.
   *
   * @param params the file and how to import it
   * @param opts per-call options
   * @return the preview
   */
  public JsonNode importPreview(ImportContactsParams params, RequestOptions opts) {
    return upload("/contacts/import/preview", params, opts);
  }

  /**
   * Every contact as CSV (re-importable) or vCard 4.0 (format {@code vcf}).
   *
   * @param params the format, or null for CSV
   * @return the file's text
   */
  public String export(ExportContactsParams params) {
    return export(params, null);
  }

  /**
   * An export, with per-call options.
   *
   * @param params the format, or null for CSV
   * @param opts per-call options
   * @return the file's text
   */
  public String export(ExportContactsParams params, RequestOptions opts) {
    ApiRequest req = ApiRequest.get("/contacts/export");
    if (params != null) {
      req.query("format", params.getFormat()).query("audience", params.getAudience());
    }
    return core.text(req, opts);
  }

  private JsonNode upload(String path, ImportContactsParams p, RequestOptions opts) {
    String name = p.getFilename() == null || p.getFilename().isEmpty() ? "contacts.csv" : p.getFilename();
    String type = name.toLowerCase(Locale.ROOT).endsWith(".vcf") ? "text/vcard" : "text/csv";
    Multipart form =
        new Multipart()
            .field("audienceId", p.getAudienceId())
            .field("newAudience", p.getNewAudience())
            .field(
                "createAudiences",
                p.getCreateAudiences() == null ? null : p.getCreateAudiences().toString())
            .field("onDuplicate", p.getOnDuplicate());
    if (p.getMapping() != null && !p.getMapping().isEmpty()) {
      form.field("mapping", Json.writeString(p.getMapping()));
    }
    form.file("file", name, type, p.getFile() == null ? new byte[0] : p.getFile());
    return core.json(ApiRequest.post(path).form(form), opts, JsonNode.class);
  }
}
