package com.sendclipper.model;

/** One submitted item's outcome: {@code accepted} (id and messageId set) or {@code failed} (error set). */
public class BatchResult extends ApiObject {
  private int index;
  private String status;
  private String id;
  private String messageId;
  private BatchItemError error;

  /**
   * The item's position in the request.
   *
   * @return the index
   */
  public int getIndex() {
    return index;
  }

  /**
   * {@code accepted} or {@code failed}.
   *
   * @return the status
   */
  public String getStatus() {
    return status;
  }

  public String getId() {
    return id;
  }

  public String getMessageId() {
    return messageId;
  }

  /**
   * Why the item was refused, or null.
   *
   * @return the error
   */
  public BatchItemError getError() {
    return error;
  }

  /**
   * Whether the item was accepted.
   *
   * @return true for {@code accepted}
   */
  public boolean isAccepted() {
    return "accepted".equals(status);
  }

}
