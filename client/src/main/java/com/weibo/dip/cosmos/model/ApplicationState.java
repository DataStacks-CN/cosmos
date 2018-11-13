package com.weibo.dip.cosmos.model;

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
