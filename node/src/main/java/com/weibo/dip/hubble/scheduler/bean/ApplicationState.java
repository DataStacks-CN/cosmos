package com.weibo.dip.hubble.scheduler.bean;

/**
 * Application state.
 *
 * @author yurun
 */
public enum ApplicationState {
  QUEUED,
  PENDING,
  RUNNING,
  SUCCESS,
  FAILED,
  KILLED
}
