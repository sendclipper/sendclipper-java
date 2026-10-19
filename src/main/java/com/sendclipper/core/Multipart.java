package com.sendclipper.core;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/** A multipart/form-data body. Internal: not part of the public API. */
public final class Multipart {
  private static final SecureRandom RANDOM = new SecureRandom();

  private final String boundary;
  private final ByteArrayOutputStream out = new ByteArrayOutputStream();

  /** Starts an empty form with a random boundary. */
  public Multipart() {
    byte[] b = new byte[16];
    RANDOM.nextBytes(b);
    StringBuilder sb = new StringBuilder("sendclipper-");
    for (byte x : b) {
      sb.append(String.format("%02x", x));
    }
    this.boundary = sb.toString();
  }

  /**
   * Adds a text field; a null or empty value is left out.
   *
   * @param name the field name
   * @param value its value
   * @return this form
   */
  public Multipart field(String name, String value) {
    if (value == null || value.isEmpty()) {
      return this;
    }
    write("--" + boundary + "\r\n");
    write("Content-Disposition: form-data; name=\"" + quote(name) + "\"\r\n\r\n");
    write(value);
    write("\r\n");
    return this;
  }

  /**
   * Adds a file.
   *
   * @param name the field name
   * @param filename the file's name
   * @param contentType its MIME type
   * @param content its bytes
   * @return this form
   */
  public Multipart file(String name, String filename, String contentType, byte[] content) {
    write("--" + boundary + "\r\n");
    write(
        "Content-Disposition: form-data; name=\""
            + quote(name)
            + "\"; filename=\""
            + quote(filename)
            + "\"\r\n");
    write("Content-Type: " + contentType + "\r\n\r\n");
    out.writeBytes(content);
    write("\r\n");
    return this;
  }

  byte[] bytes() {
    ByteArrayOutputStream copy = new ByteArrayOutputStream();
    copy.writeBytes(out.toByteArray());
    copy.writeBytes(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
    return copy.toByteArray();
  }

  String contentType() {
    return "multipart/form-data; boundary=" + boundary;
  }

  private void write(String s) {
    out.writeBytes(s.getBytes(StandardCharsets.UTF_8));
  }

  private static String quote(String s) {
    return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", " ").replace("\n", " ");
  }
}
