package com.weibo.dip.cosmos.model;

import com.google.gson.JsonObject;
import com.weibo.dip.cosmos.Conf;
import com.weibo.dip.durian.Symbols;
import com.weibo.dip.durian.util.GsonUtil;
import java.io.Serializable;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application.
 *
 * @author yurun
 */
public class Application implements Serializable {
  public static final String EVENT_DRIVEN = "event-driven";

  private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

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

  public Application() {}

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

  public JsonObject getJsonParams() {
    return GsonUtil.parse(params).getAsJsonObject();
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

  /**
   * Get application parallelism.
   *
   * @return application parallelism
   */
  public int getParallelism() {
    try {
      return getJsonParams().has(Conf.COSMOS_APP_PARALLELISM)
          ? getJsonParams().getAsJsonPrimitive(Conf.COSMOS_APP_PARALLELISM).getAsInt()
          : Integer.MAX_VALUE;
    } catch (Exception e) {
      LOGGER.error(
          "Application {} get paralellism error: {}",
          getUniqeName(),
          ExceptionUtils.getFullStackTrace(e));

      return Integer.MAX_VALUE;
    }
  }

  /**
   * Get application disk space.
   *
   * @return application disk space
   */
  public int getDiskSpace() {
    try {
      return getJsonParams().has(Conf.COSMOS_APP_DISK_SPACE)
          ? getJsonParams().getAsJsonPrimitive(Conf.COSMOS_APP_DISK_SPACE).getAsInt()
          : 0;
    } catch (Exception e) {
      LOGGER.error(
          "Application {} get disk space error: {}",
          getUniqeName(),
          ExceptionUtils.getFullStackTrace(e));

      return 0;
    }
  }

  /**
   * Get application net flow.
   *
   * @return application net flow
   */
  public int getNetFlow() {
    try {
      return getJsonParams().has(Conf.COSMOS_APP_NET_FLOW)
          ? getJsonParams().getAsJsonPrimitive(Conf.COSMOS_APP_NET_FLOW).getAsInt()
          : 0;
    } catch (Exception e) {
      LOGGER.error(
          "Application {} get net flow error: {}",
          getUniqeName(),
          ExceptionUtils.getFullStackTrace(e));

      return 0;
    }
  }

  @Override
  public String toString() {
    return "Application{"
        + "name='"
        + name
        + '\''
        + ", queue='"
        + queue
        + '\''
        + ", user='"
        + user
        + '\''
        + ", priority="
        + priority
        + ", cores="
        + cores
        + ", mems="
        + mems
        + ", repository='"
        + repository
        + '\''
        + ", tag='"
        + tag
        + '\''
        + ", params='"
        + params
        + '\''
        + ", cron='"
        + cron
        + '\''
        + ", timeout="
        + timeout
        + '}';
  }
}
