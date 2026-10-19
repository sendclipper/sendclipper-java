package com.sendclipper.model;

/** What a download returns. */
public final class DownloadedFile {
  private final byte[] content;
  private final String contentType;
  private final String filename;

  /**
   * Makes a file. The SDK makes these; you need this constructor only to fake one in a test.
   *
   * @param content the bytes
   * @param contentType the MIME type, or ""
   * @param filename the name from Content-Disposition, or ""
   */
  public DownloadedFile(byte[] content, String contentType, String filename) {
    this.content = content;
    this.contentType = contentType;
    this.filename = filename;
  }

  /**
   * The file's bytes.
   *
   * @return the bytes (not copied)
   */
  public byte[] getContent() {
    return content;
  }

  /**
   * The MIME type the API sent.
   *
   * @return the type, or ""
   */
  public String getContentType() {
    return contentType;
  }

  /**
   * The filename the API sent, decoded.
   *
   * @return the name, or ""
   */
  public String getFilename() {
    return filename;
  }

  @Override
  public String toString() {
    return "DownloadedFile{filename=" + filename + ", contentType=" + contentType + ", bytes=" + content.length + "}";
  }
}
