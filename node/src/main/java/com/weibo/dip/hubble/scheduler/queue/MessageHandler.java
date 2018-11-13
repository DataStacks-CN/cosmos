package com.weibo.dip.hubble.scheduler.queue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.dbutils.ResultSetHandler;

public class MessageHandler implements ResultSetHandler<List<Message>> {
  @Override
  public List<Message> handle(ResultSet rs) throws SQLException {
    List<Message> messages = null;

    while (rs.next()) {
      if (messages == null) {
        messages = new ArrayList<>();
      }

      Message message = new Message();

      message.setId(rs.getInt("id"));
      message.setQueue(rs.getString("queue"));
      message.setPriority(rs.getInt("priority"));
      message.setMessage(rs.getString("msgcontent"));
      message.setTimestamp(new Date(rs.getTimestamp("msgtimestamp").getTime()));

      messages.add(message);
    }

    return messages;
  }
}
