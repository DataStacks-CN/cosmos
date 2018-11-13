package com.weibo.dip.cosmos.node.db;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.weibo.dip.cosmos.common.ClasspathProperties;

/**
 * Scheduler datasource factory.
 *
 * @author yurun
 */
public class SchedulerDataSourceFactory {
  private ClasspathProperties properties;

  /**
   * Initialize node datasource factory.
   *
   * @param properties classpath properties
   */
  public void initialize(ClasspathProperties properties) {
    this.properties = properties;
  }

  /**
   * Get node datasource.
   *
   * @return a node datasource
   * @throws Exception if create datasource error
   */
  public SchedulerDataSource getDataSource() throws Exception {
    ComboPooledDataSource dataSource = new ComboPooledDataSource();

    dataSource.setDriverClass(properties.getString("c3p0.driverClass"));
    dataSource.setJdbcUrl(properties.getString("c3p0.jdbcUrl"));
    dataSource.setUser(properties.getString("c3p0.user"));
    dataSource.setPassword(properties.getString("c3p0.password"));
    dataSource.setAcquireIncrement(properties.getInt("c3p0.acquireIncrement"));
    dataSource.setInitialPoolSize(properties.getInt("c3p0.initialPoolSize"));
    dataSource.setMaxPoolSize(properties.getInt("c3p0.maxPoolSize"));
    dataSource.setMaxIdleTime(properties.getInt("c3p0.maxIdleTime"));
    dataSource.setMinPoolSize(properties.getInt("c3p0.minPoolSize"));
    dataSource.setPreferredTestQuery(properties.getString("c3p0.preferredTestQuery"));
    dataSource.setTestConnectionOnCheckout(properties.getBoolean("c3p0.testConnectionOnCheckout"));
    dataSource.setAcquireRetryAttempts(properties.getInt("c3p0.acquireRetryAttempts"));
    dataSource.setAcquireRetryDelay(properties.getInt("c3p0.acquireRetryDelay"));
    dataSource.setCheckoutTimeout(properties.getInt("c3p0.checkoutTimeout"));

    return new SchedulerDataSource(dataSource);
  }
}
