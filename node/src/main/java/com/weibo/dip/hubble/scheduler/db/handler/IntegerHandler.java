package com.weibo.dip.hubble.scheduler.db.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.commons.dbutils.ResultSetHandler;

/**
 * QueryRunner integer handler.
 *
 * @author yurun
 */
public class IntegerHandler implements ResultSetHandler<Integer> {
  @Override
  public Integer handle(ResultSet rs) throws SQLException {
    if (rs.next()) {
      return rs.getInt(1);
    }

    return 0;
  }
}
