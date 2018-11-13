package com.weibo.dip.hubble.scheduler.queue;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * Message.
 *
 * @author yurun
 */
public class Message implements Serializable {
  private int id;
  private String queue;
  private int priority;
  private String message;
  private Date timestamp;

  public Message() {}

  /**
   * Construct a message instance.
   *
   * @param queue queue name
   * @param priority the lower the value, the higher the priority
   * @param message message info(str)
   * @param timestamp from this timestamp, message can be acquired
   */
  public Message(String queue, int priority, String message, Date timestamp) {
    this.queue = queue;
    this.priority = priority;
    this.message = message;
    this.timestamp = timestamp;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getQueue() {
    return queue;
  }

  public void setQueue(String queue) {
    this.queue = queue;
  }

  public int getPriority() {
    return priority;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Message message = (Message) o;

    return id == message.id;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "Message{"
        + "id="
        + id
        + ", queue='"
        + queue
        + '\''
        + ", priority="
        + priority
        + ", message='"
        + message
        + '\''
        + ", timestamp="
        + timestamp
        + '}';
  }
}
