package com.weibo.dip.cosmos.node.db.handler;

import com.weibo.dip.cosmos.model.Application;
import com.weibo.dip.durian.Symbols;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;

public class ApplicationHandler implements ResultSetHandler<List<Application>> {
  @Override
  public List<Application> handle(ResultSet rs) throws SQLException {
    List<Application> applications = null;

    while (rs.next()) {
      if (applications == null) {
        applications = new ArrayList<>();
      }

      Application application = new Application();

      application.setName(rs.getString("name"));
      application.setQueue(rs.getString("queue"));
      application.setUser(rs.getString("user"));
      application.setPriority(rs.getInt("priority"));
      application.setCores(rs.getInt("cores"));
      application.setMems(rs.getInt("mems"));
      application.setRepository(rs.getString("repository"));
      application.setTag(rs.getString("tag"));

      String paramsStr = rs.getString("params");
      if (StringUtils.isNotEmpty(paramsStr)) {
        application.setParams(paramsStr.split(Symbols.SPACE, -1));
      } else {
        application.setParams(null);
      }

      application.setCron(rs.getString("cron"));
      application.setTimeout(rs.getInt("timeout"));

      applications.add(application);
    }

    return applications;
  }
}
