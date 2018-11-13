package com.weibo.dip.cosmos.node.quartz;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.weibo.dip.cosmos.node.db.SchedulerDataSource;
import com.weibo.dip.durian.GlobalResource;
import java.sql.Connection;
import java.sql.SQLException;
import org.quartz.utils.ConnectionProvider;

/** Quartz connection provider. */
public class QuartzConnectionProvider implements ConnectionProvider {
  private ComboPooledDataSource dataSource =
      GlobalResource.get(SchedulerDataSource.class).getDataSource();

  @Override
  public Connection getConnection() throws SQLException {
    return dataSource.getConnection();
  }

  @Override
  public void shutdown() {}

  @Override
  public void initialize() {}
}
