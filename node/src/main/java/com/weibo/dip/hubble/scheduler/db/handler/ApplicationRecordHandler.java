package com.weibo.dip.hubble.scheduler.db.handler;

import com.weibo.dip.hubble.scheduler.bean.ApplicationRecord;
import com.weibo.dip.hubble.scheduler.bean.ApplicationState;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.dbutils.ResultSetHandler;

/**
 * Application record handler.
 *
 * @author yurun
 */
public class ApplicationRecordHandler implements ResultSetHandler<List<ApplicationRecord>> {
  @Override
  public List<ApplicationRecord> handle(ResultSet rs) throws SQLException {
    List<ApplicationRecord> records = null;

    while (rs.next()) {
      if (records == null) {
        records = new ArrayList<>();
      }

      ApplicationRecord record = new ApplicationRecord();

      record.setName(rs.getString("name"));
      record.setQueue(rs.getString("queue"));
      record.setHost(rs.getString("host"));
      record.setScheduleTime(new Date(rs.getTimestamp("scheduletime").getTime()));
      record.setUpdateTime(new Date(rs.getTimestamp("updatetime").getTime()));

      int state = rs.getInt("state");

      for (ApplicationState applicationState : ApplicationState.values()) {
        if (applicationState.ordinal() == state) {
          record.setState(applicationState);

          break;
        }
      }

      records.add(record);
    }

    return records;
  }
}
