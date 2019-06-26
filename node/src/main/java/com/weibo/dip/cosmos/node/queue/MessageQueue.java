package com.weibo.dip.cosmos.node.queue;

import com.weibo.dip.cosmos.model.Message;
import com.weibo.dip.cosmos.node.db.SchedulerDataSource;
import com.weibo.dip.cosmos.node.db.handler.LongHandler;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.sql.DataSource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Message queue based mysql.
 *
 * @author yurun
 */
public class MessageQueue {
  private static final Logger LOGGER = LoggerFactory.getLogger(MessageQueue.class);

  private static final String DEFAULT_QUEUE = "default";
  private static final int DEFAULT_PRIORITY = 0;

  private DataSource dataSource;
  private QueryRunner queryRunner;

  public MessageQueue(SchedulerDataSource schedulerDataSource) {
    dataSource = schedulerDataSource.getDataSource();
    queryRunner = new QueryRunner(dataSource);
  }

  public boolean produce(Message message) throws Exception {
    return produce(
        message.getMessage(), message.getQueue(), message.getPriority(), message.getTimestamp());
  }

  /**
   * Produce a message instance.
   *
   * @param queue queue name
   * @param priority the lower the value, the higher the priority
   * @param message message info(str)
   * @param timestamp from this timestamp, message can be acquired
   */
  public boolean produce(String message, String queue, int priority, Date timestamp)
      throws Exception {
    String sql =
        "insert into message_queue"
            + " ("
            + "msgcontent, queue, priority, msgtimestamp"
            + ") "
            + "values"
            + " ("
            + "?, ?, ?, ?"
            + ")";

    return queryRunner.update(sql, message, queue, priority, timestamp) > 0;
  }

  public boolean produce(String message, String queue, int priority) throws Exception {
    return produce(message, queue, priority, new Date());
  }

  public boolean produce(String message, String queue) throws Exception {
    return produce(message, queue, DEFAULT_PRIORITY);
  }

  public boolean produce(String message) throws Exception {
    return produce(message, DEFAULT_QUEUE, DEFAULT_PRIORITY);
  }

  public Message consume(String queue) throws Exception {
    return consume(queue, new Date());
  }

  /**
   * Consume a message from the specified queue after the specified timestamp.
   *
   * @param queue queue
   * @param timestamp timestamp
   * @return message
   * @throws Exception if access db error
   */
  public Message consume(String queue, Date timestamp) throws Exception {

    Message message;

    String consumer = UUID.randomUUID().toString();

    try (Connection conn = dataSource.getConnection()) {

      String querySql =
          "select * from message_queue"
              + " where queue = ? and msgtimestamp <= ? and consumer = ''"
              + " order by priority asc, msgtimestamp asc"
              + " limit 1";

      message = queryRunner.query(conn, querySql, new MessageHandler(), queue, timestamp).get(0);

      if (Objects.isNull(message)) {
        return null;
      }

      try {

        conn.setAutoCommit(false);

        String updateSql = "update message_queue set consumer = ? where id = ?";

        int rows = queryRunner.update(conn, updateSql, consumer, message.getId());

        if (rows == 1) {

          String deleteSql = "delete from message_queue where id = ?";

          queryRunner.update(conn, deleteSql, message.getId());
        } else {

          message = null;
        }

        conn.commit();
      } catch (Exception e) {
        conn.rollback();

        throw e;
      }

    }

    return message;
  }

  /**
   * Get a message by id.
   *
   * @param id id
   * @return message
   * @throws Exception if access db error
   */
  public Message get(int id) throws Exception {
    String sql = "select * from message_queue where id = ? and consumer = ''";

    List<Message> messages = queryRunner.query(sql, new MessageHandler(), id);

    return CollectionUtils.isNotEmpty(messages) ? messages.get(0) : null;
  }

  /**
   * Get the number of all messages.
   *
   * @return number
   * @throws Exception if access db error
   */
  public int queued() throws Exception {
    String sql = "select count(1) from message_queue";

    return queryRunner.query(sql, new LongHandler()).intValue();
  }

  /**
   * Get all messages.
   *
   * @return messages
   * @throws Exception if access db error
   */
  public List<Message> listQueued() throws Exception {
    String sql = "select * from message_queue order by priority asc, msgtimestamp asc";

    return queryRunner.query(sql, new MessageHandler());
  }

  /**
   * Update the priority of the specified message.
   *
   * @param id message id
   * @param priority priority value
   * @return true, update successful; otherwise failed
   * @throws Exception if access db error
   */
  public boolean updateQueuedPriority(int id, int priority) throws Exception {
    String sql = "update message_queue set priority = ? where id = ?";

    return queryRunner.update(sql, priority, id) > 0;
  }

  /**
   * Delete the specifiled message.
   *
   * @param id message id
   * @return true, delete successful; otherwise failed
   * @throws Exception if access db error
   */
  public boolean deleteQueued(int id) throws Exception {
    String sql = "delete from message_queue where id = ? and consumer = ''";

    return queryRunner.update(sql, id) > 0;
  }

  /**
   * Get all queues.
   *
   * @return queues
   * @throws Exception if access db error
   */
  public List<String> queues() throws Exception {
    String sql = "select distinct(queue) from message_queue";

    return queryRunner.query(sql, new StringHandler());
  }
}
