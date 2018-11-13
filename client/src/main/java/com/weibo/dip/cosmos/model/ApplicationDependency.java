package com.weibo.dip.cosmos.model;

import java.io.Serializable;

/**
 * Application dependency.
 *
 * @author yurun
 */
public class ApplicationDependency implements Serializable {
  private String name;
  private String queue;

  private String dependName;
  private String dependQueue;

  private int fromSeconds;
  private int toSeconds;

  public ApplicationDependency() {}

  /**
   * Construct a instance.
   *
   * @param name app name
   * @param queue app queue
   * @param dependName depend app name
   * @param dependQueue depend app queue
   * @param fromSeconds the starting seconds which to find fire time
   * @param toSeconds the ending seconds which to stoping find fire time
   */
  public ApplicationDependency(
      String name,
      String queue,
      String dependName,
      String dependQueue,
      int fromSeconds,
      int toSeconds) {
    this.name = name;
    this.queue = queue;
    this.dependName = dependName;
    this.dependQueue = dependQueue;
    this.fromSeconds = fromSeconds;
    this.toSeconds = toSeconds;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getQueue() {
    return queue;
  }

  public void setQueue(String queue) {
    this.queue = queue;
  }

  public String getDependName() {
    return dependName;
  }

  public void setDependName(String dependName) {
    this.dependName = dependName;
  }

  public String getDependQueue() {
    return dependQueue;
  }

  public void setDependQueue(String dependQueue) {
    this.dependQueue = dependQueue;
  }

  public int getFromSeconds() {
    return fromSeconds;
  }

  public void setFromSeconds(int fromSeconds) {
    this.fromSeconds = fromSeconds;
  }

  public int getToSeconds() {
    return toSeconds;
  }

  public void setToSeconds(int toSeconds) {
    this.toSeconds = toSeconds;
  }
}
