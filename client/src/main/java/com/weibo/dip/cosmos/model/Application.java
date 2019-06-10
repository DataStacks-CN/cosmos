package com.weibo.dip.cosmos.model;

import com.weibo.dip.durian.Symbols;
import java.io.Serializable;

/**
 * Application.
 *
 * @author yurun
 */
public class Application implements Serializable {

  private String name;
  private String queue;
  private String user;
  private int priority;
  private int cores;
  private int mems;
  private String repository;
  private String tag;
  private String params;
  private String cron;
  private int timeout;

  public Application() {
  }

  /**
   * Construct a application instance.
   *
   * @param name name
   * @param queue queue
   * @param user user
   * @param priority priority
   * @param cores cores
   * @param mems mems(MB)
   * @param repository docker image repository
   * @param tag docker image tag
   * @param cron quartz cron
   * @param timeout application run timeout
   */
  public Application(
      String name,
      String queue,
      String user,
      int priority,
      int cores,
      int mems,
      String repository,
      String tag,
      String params,
      String cron,
      int timeout) {
    this.name = name;
    this.queue = queue;
    this.user = user;
    this.priority = priority;
    this.cores = cores;
    this.mems = mems;
    this.repository = repository;
    this.tag = tag;
    this.params = params;
    this.cron = cron;
    this.timeout = timeout;
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

  public String getUniqeName() {
    return name + Symbols.LINE_THROUGH + queue;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public int getPriority() {
    return priority;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  public int getCores() {
    return cores;
  }

  public void setCores(int cores) {
    this.cores = cores;
  }

  public int getMems() {
    return mems;
  }

  public void setMems(int mems) {
    this.mems = mems;
  }

  public String getRepository() {
    return repository;
  }

  public void setRepository(String repository) {
    this.repository = repository;
  }

  public String getTag() {
    return tag;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }

  public String getParams() {
    return params;
  }

  public void setParams(String params) {
    this.params = params;
  }

  public String getCron() {
    return cron;
  }

  public void setCron(String cron) {
    this.cron = cron;
  }

  public int getTimeout() {
    return timeout;
  }

  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }

  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer("Application{");
    sb.append("name='").append(name).append('\'');
    sb.append(", queue='").append(queue).append('\'');
    sb.append(", user='").append(user).append('\'');
    sb.append(", priority=").append(priority);
    sb.append(", cores=").append(cores);
    sb.append(", mems=").append(mems);
    sb.append(", repository='").append(repository).append('\'');
    sb.append(", tag='").append(tag).append('\'');
    sb.append(", params='").append(params).append('\'');
    sb.append(", cron='").append(cron).append('\'');
    sb.append(", timeout=").append(timeout);
    sb.append('}');
    return sb.toString();
  }
}
