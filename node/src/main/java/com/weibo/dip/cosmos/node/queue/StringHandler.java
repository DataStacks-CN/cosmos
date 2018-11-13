package com.weibo.dip.cosmos.node.queue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.dbutils.ResultSetHandler;

/**
 * Message count handler.
 *
 * @author yurun
 */
public class StringHandler implements ResultSetHandler<List<String>> {
  @Override
  public List<String> handle(ResultSet rs) throws SQLException {
    List<String> queues = null;

    while (rs.next()) {
      if (Objects.isNull(queues)) {
        queues = new ArrayList<>();
      }

      queues.add(rs.getString(1));
    }

    return queues;
  }
}
