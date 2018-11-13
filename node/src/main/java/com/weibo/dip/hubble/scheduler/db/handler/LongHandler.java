package com.weibo.dip.hubble.scheduler.db.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.commons.dbutils.ResultSetHandler;

/**
 * QueryRunner long handler.
 *
 * @author yurun
 */
public class LongHandler implements ResultSetHandler<Long> {
  @Override
  public Long handle(ResultSet rs) throws SQLException {
    if (rs.next()) {
      return rs.getLong(1);
    }

    return 0L;
  }
}
