package com.weibo.dip.hubble.scheduler.db;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import java.io.Closeable;

/**
 * Scheduler datasource based ComboPooledDataSource, support close.
 *
 * @author yurun
 */
public class SchedulerDataSource implements Closeable {
  private ComboPooledDataSource dataSource;

  public SchedulerDataSource(ComboPooledDataSource dataSource) {
    this.dataSource = dataSource;
  }

  public ComboPooledDataSource getDataSource() {
    return dataSource;
  }

  @Override
  public void close() {
    dataSource.close();
  }
}
