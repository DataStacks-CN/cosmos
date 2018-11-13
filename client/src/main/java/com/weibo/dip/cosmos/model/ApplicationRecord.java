package com.weibo.dip.cosmos.model;

import com.weibo.dip.durian.Symbols;
import com.weibo.dip.durian.util.DatetimeUtil;
import java.io.Serializable;
import java.util.Date;

/**
 * Application record.
 *
 * @author yurun
 */
public class ApplicationRecord implements Serializable {
  private String name;
  private String queue;
  private String host;
  private Date scheduleTime;
  private Date updateTime;
  private ApplicationState state;

  public ApplicationRecord() {}

  /**
   * Construct a instance.
   *
   * @param name application name
   * @param queue application queue
   * @param scheduleTime node time
   * @param updateTime update time
   * @param state state
   */
  public ApplicationRecord(
      String name,
      String queue,
      String host,
      Date scheduleTime,
      Date updateTime,
      ApplicationState state) {
    this.name = name;
    this.queue = queue;
    this.host = host;
    this.scheduleTime = scheduleTime;
    this.updateTime = updateTime;
    this.state = state;
  }

  public String getName() {
    return name;
  }

  /**
   * Get a uniqe name from record.
   *
   * @return uniqe name
   */
  public String getUniqeName() {
    return name
        + Symbols.UNDERLINE
        + queue
        + Symbols.UNDERLINE
        + DatetimeUtil.DATETIME_FORMAT.format(scheduleTime);
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

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public Date getScheduleTime() {
    return scheduleTime;
  }

  public void setScheduleTime(Date scheduleTime) {
    this.scheduleTime = scheduleTime;
  }

  public Date getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(Date updateTime) {
    this.updateTime = updateTime;
  }

  public ApplicationState getState() {
    return state;
  }

  public void setState(ApplicationState state) {
    this.state = state;
  }
}
