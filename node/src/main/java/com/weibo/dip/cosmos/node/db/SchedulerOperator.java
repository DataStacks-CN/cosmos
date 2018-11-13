package com.weibo.dip.cosmos.node.db;

import com.weibo.dip.cosmos.model.Application;
import com.weibo.dip.cosmos.model.ApplicationDependency;
import com.weibo.dip.cosmos.model.ApplicationRecord;
import com.weibo.dip.cosmos.model.ApplicationState;
import com.weibo.dip.cosmos.node.db.handler.ApplicationDependencyHandler;
import com.weibo.dip.cosmos.node.db.handler.ApplicationHandler;
import com.weibo.dip.cosmos.node.db.handler.ApplicationRecordHandler;
import com.weibo.dip.durian.Symbols;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/** Scheduler db operator. */
public class SchedulerOperator {
  private QueryRunner queryRunner;

  public SchedulerOperator(SchedulerDataSource schedulerDataSource) {
    queryRunner = new QueryRunner(schedulerDataSource.getDataSource());
  }

  /**
   * Add an application.
   *
   * @param application application
   * @return the number rows of added
   * @throws SQLException if access db error
   */
  public int addApplication(Application application) throws SQLException {
    List<String> sqls = new ArrayList<>();

    sqls.add("insert into applications");
    sqls.add("(name, queue, user, priority, cores, mems, repository, tag, params, cron, timeout)");
    sqls.add("values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

    String sql = StringUtils.join(sqls, Symbols.SPACE);

    String paramsStr = "";

    String[] params = application.getParams();
    if (ArrayUtils.isNotEmpty(params)) {
      paramsStr = StringUtils.join(params, Symbols.SPACE);
    }

    return queryRunner.update(
        sql,
        application.getName(),
        application.getQueue(),
        application.getUser(),
        application.getPriority(),
        application.getCores(),
        application.getMems(),
        application.getRepository(),
        application.getTag(),
        paramsStr,
        application.getCron(),
        application.getTimeout());
  }

  /**
   * Delete the application with the specified name and queue.
   *
   * @param name application name
   * @param queue application queue
   * @return the number rows of deleted
   * @throws SQLException if access db error
   */
  public int deleteApplication(String name, String queue) throws SQLException {
    String sql = "delete from applications where name = ? and queue = ?";

    return queryRunner.update(sql, name, queue);
  }

  /**
   * Get the application of the specified name and queue.
   *
   * @param name application name
   * @param queue application queue
   * @return application
   * @throws SQLException if access db error
   */
  public Application getApplication(String name, String queue) throws SQLException {
    String sql = "select * from applications where name = ? and queue = ?";

    List<Application> applications = queryRunner.query(sql, new ApplicationHandler(), name, queue);

    if (CollectionUtils.isNotEmpty(applications)) {
      return applications.get(0);
    } else {
      return null;
    }
  }

  /**
   * Update application.
   *
   * @param application application
   * @return the number rows of added
   * @throws SQLException if access db error
   */
  public int updateApplication(Application application) throws SQLException {
    List<String> sqls = new ArrayList<>();

    sqls.add("update applications ");
    sqls.add("set user = ?, ");
    sqls.add("priority = ?, ");
    sqls.add("cores = ?, ");
    sqls.add("mems = ?, ");
    sqls.add("repository = ?, ");
    sqls.add("tag = ?, ");
    sqls.add("params = ?, ");
    sqls.add("cron = ?, ");
    sqls.add("timeout = ? ");
    sqls.add("where name = ? ");
    sqls.add("and queue = ?");

    String sql = StringUtils.join(sqls, Symbols.SPACE);

    String paramsStr = "";

    String[] params = application.getParams();
    if (ArrayUtils.isNotEmpty(params)) {
      paramsStr = StringUtils.join(params, Symbols.SPACE);
    }

    return queryRunner.update(
        sql,
        application.getUser(),
        application.getPriority(),
        application.getCores(),
        application.getMems(),
        application.getRepository(),
        application.getTag(),
        paramsStr,
        application.getCron(),
        application.getTimeout(),
        application.getName(),
        application.getQueue());
  }

  /**
   * Whether an application already exists.
   *
   * @param name application name
   * @param queue application queue
   * @return true if exists, else false
   * @throws SQLException if access db error
   */
  public boolean existApplication(String name, String queue) throws SQLException {
    return Objects.nonNull(getApplication(name, queue));
  }

  /**
   * Add or update application record.
   *
   * @param record application record
   * @return the number of rows updated
   * @throws SQLException if access db error
   */
  public int addOrUpdateApplicationRecord(ApplicationRecord record) throws SQLException {
    List<String> sqls = new ArrayList<>();

    sqls.add("insert into records");
    sqls.add("(name, queue, host, scheduletime, updatetime, state)");
    sqls.add("values (?, ?, ?, ?, ?, ?)");
    sqls.add("on duplicate key update");
    sqls.add("host = values(host),");
    sqls.add("updatetime = values(updatetime),");
    sqls.add("state = values(state)");

    String sql = StringUtils.join(sqls, Symbols.SPACE);

    return queryRunner.update(
        sql,
        record.getName(),
        record.getQueue(),
        record.getHost(),
        record.getScheduleTime(),
        record.getUpdateTime(),
        record.getState().ordinal());
  }

  /**
   * Get the used cores of the specified queue.
   *
   * @param queue queue
   * @return used cores
   * @throws SQLException if access db error
   */
  public int getUsedCores(String queue) throws SQLException {
    List<ApplicationRecord> runningApplicationRecords = getRunningApplicationRecords(queue);
    if (CollectionUtils.isEmpty(runningApplicationRecords)) {
      return 0;
    }

    int cores = 0;

    for (ApplicationRecord runningApplicationRecord : runningApplicationRecords) {
      Application application =
          getApplication(runningApplicationRecord.getName(), runningApplicationRecord.getQueue());

      cores += application.getCores();
    }

    return cores;
  }

  /**
   * Get the used mems of the specified queue.
   *
   * @param queue queue
   * @return used mems
   * @throws SQLException if access db error
   */
  public int getUsedMems(String queue) throws SQLException {
    List<ApplicationRecord> runningApplicationRecords = getRunningApplicationRecords(queue);
    if (CollectionUtils.isEmpty(runningApplicationRecords)) {
      return 0;
    }

    int mems = 0;

    for (ApplicationRecord runningApplicationRecord : runningApplicationRecords) {
      Application application =
          getApplication(runningApplicationRecord.getName(), runningApplicationRecord.getQueue());

      mems += application.getMems();
    }

    return mems;
  }

  /**
   * Get the application record of the specified name, queue and schedule time.
   *
   * @param name application name
   * @param queue application queue
   * @param scheduleTime schedule time
   * @return application record
   * @throws SQLException if access db error
   */
  public ApplicationRecord getApplicationRecord(String name, String queue, Date scheduleTime)
      throws SQLException {
    String sql = "select * from records where name = ? and queue = ? and scheduletime = ?";

    List<ApplicationRecord> records =
        queryRunner.query(sql, new ApplicationRecordHandler(), name, queue, scheduleTime);
    if (CollectionUtils.isNotEmpty(records)) {
      return records.get(0);
    } else {
      return null;
    }
  }

  /**
   * Get the application records of the specified name, queue and schedule time range.
   *
   * @param name application name
   * @param queue application queue
   * @param beginTime begin time
   * @param endTime end time
   * @return application records
   * @throws SQLException if access db error
   */
  public List<ApplicationRecord> getApplicationRecords(
      String name, String queue, Date beginTime, Date endTime) throws SQLException {
    String sql =
        "select * from records where name = ? and queue = ? and scheduleTime between ? and ?";

    return queryRunner.query(sql, new ApplicationRecordHandler(), name, queue, beginTime, endTime);
  }

  /**
   * Get running application records of the specified queue.
   *
   * @param queue queue
   * @return running application records
   * @throws SQLException if access db error
   */
  public List<ApplicationRecord> getRunningApplicationRecords(String queue) throws SQLException {
    String sql = "select * from records where queue = ? and state = ?";

    return queryRunner.query(
        sql, new ApplicationRecordHandler(), queue, ApplicationState.RUNNING.ordinal());
  }

  /**
   * Add or update a application dependency.
   *
   * @param dependency application dependency
   * @return the number of rows updated
   * @throws SQLException if access db error
   */
  public int addOrUpdateApplicationDependency(ApplicationDependency dependency)
      throws SQLException {
    List<String> sqls = new ArrayList<>();

    sqls.add("insert into dependencies");
    sqls.add("(name, queue, depend_name, depend_queue, from_seconds, to_seconds)");
    sqls.add("values (?, ?, ?, ?, ?, ?)");
    sqls.add("on duplicate key update");
    sqls.add("from_seconds = values(from_seconds),");
    sqls.add("to_seconds = values(to_seconds)");

    String sql = StringUtils.join(sqls, Symbols.SPACE);

    return queryRunner.update(
        sql,
        dependency.getName(),
        dependency.getQueue(),
        dependency.getDependName(),
        dependency.getDependQueue(),
        dependency.getFromSeconds(),
        dependency.getToSeconds());
  }

  /**
   * Delete a application depencency.
   *
   * @param name application name
   * @param queue application queue
   * @param dependName depend application name
   * @param dependQueue depend application queue
   * @return the number of rows updated
   * @throws SQLException if access db error
   */
  public int deleteApplicationDependency(
      String name, String queue, String dependName, String dependQueue) throws SQLException {
    List<String> sqls = new ArrayList<>();

    sqls.add("delete from dependencies");
    sqls.add("where");
    sqls.add("name = ?");
    sqls.add("and queue = ?");
    sqls.add("and depend_name = ?");
    sqls.add("and depend_queue = ?");

    String sql = StringUtils.join(sqls, Symbols.SPACE);

    return queryRunner.update(sql, name, queue, dependName, dependQueue);
  }

  /**
   * Get the dependencies of the specified name and queue.
   *
   * @param name application name
   * @param queue application queue
   * @return dependencies
   * @throws SQLException if access db error
   */
  public List<ApplicationDependency> getDependencies(String name, String queue)
      throws SQLException {
    String sql = "select * from dependencies where name = ? and queue = ?";

    return queryRunner.query(sql, new ApplicationDependencyHandler(), name, queue);
  }
}
