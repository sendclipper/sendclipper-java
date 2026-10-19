package com.sendclipper.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Objects;

/**
 * One file sent with a message.
 *
 * <pre>{@code
 * Attachment.fromPath(Path.of("booking-42.pdf"))
 * Attachment.of("booking-42.pdf", bytes)
 * Attachment.of("booking-42.pdf", bytes, "application/pdf")
 * }</pre>
 */
public final class Attachment {
  private final String filename;
  @JsonIgnore private final byte[] content;
  private final String contentType;

  private Attachment(String filename, byte[] content, String contentType) {
    this.filename = Objects.requireNonNull(filename, "filename");
    this.content = Objects.requireNonNull(content, "content").clone();
    this.contentType = contentType == null || contentType.isEmpty() ? null : contentType;
  }

  /**
   * A file whose type the API guesses from the filename.
   *
   * @param filename the name the recipient sees, e.g. {@code booking-42.pdf}
   * @param content the file's raw bytes; they are base64-encoded for you
   * @return the attachment
   */
  public static Attachment of(String filename, byte[] content) {
    return new Attachment(filename, content, null);
  }

  /**
   * A file with an explicit MIME type.
   *
   * @param filename the name the recipient sees
   * @param content the file's raw bytes
   * @param contentType the MIME type, e.g. {@code application/pdf}
   * @return the attachment
   */
  public static Attachment of(String filename, byte[] content, String contentType) {
    return new Attachment(filename, content, contentType);
  }

  /**
   * Reads a file from disk; its name becomes the filename.
   *
   * @param path the file
   * @return the attachment
   * @throws UncheckedIOException when the file cannot be read
   */
  public static Attachment fromPath(Path path) {
    try {
      return new Attachment(path.getFileName().toString(), Files.readAllBytes(path), null);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public String getFilename() {
    return filename;
  }

  /**
   * The file's raw bytes.
   *
   * @return a copy of the bytes
   */
  public byte[] getContent() {
    return content.clone();
  }

  /**
   * The MIME type.
   *
   * @return the type, or null to let the API guess it from the filename
   */
  public String getContentType() {
    return contentType;
  }

  /** Standard, padded base64 (RFC 4648 section 4), which is what the API decodes. */
  @JsonProperty("content")
  private String contentBase64() {
    return Base64.getEncoder().encodeToString(content);
  }
}
