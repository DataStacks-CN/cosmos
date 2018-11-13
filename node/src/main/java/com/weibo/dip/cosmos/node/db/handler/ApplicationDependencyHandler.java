package com.weibo.dip.cosmos.node.db.handler;

import com.weibo.dip.cosmos.model.ApplicationDependency;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.dbutils.ResultSetHandler;

/**
 * Application dependency handler.
 *
 * @author yurun
 */
public class ApplicationDependencyHandler implements ResultSetHandler<List<ApplicationDependency>> {
  @Override
  public List<ApplicationDependency> handle(ResultSet rs) throws SQLException {
    List<ApplicationDependency> dependencies = null;

    while (rs.next()) {
      if (dependencies == null) {
        dependencies = new ArrayList<>();
      }

      ApplicationDependency dependency = new ApplicationDependency();

      dependency.setName(rs.getString("name"));
      dependency.setQueue(rs.getString("queue"));
      dependency.setDependName(rs.getString("depend_name"));
      dependency.setDependQueue(rs.getString("depend_queue"));
      dependency.setFromSeconds(rs.getInt("from_seconds"));
      dependency.setToSeconds(rs.getInt("to_seconds"));

      dependencies.add(dependency);
    }

    return dependencies;
  }
}
