package com.weibo.dip.cosmos.service;

import com.weibo.dip.cosmos.model.Application;
import com.weibo.dip.cosmos.model.ApplicationDependency;
import com.weibo.dip.cosmos.model.ApplicationRecord;
import com.weibo.dip.cosmos.model.Message;
import com.weibo.dip.cosmos.model.ScheduleApplication;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/** Cosmos Service. */
public interface CosmosService {
  boolean connect();

  void add(Application application) throws Exception;

  void update(Application application) throws Exception;

  void delete(String name, String queue) throws Exception;

  void start(String name, String queue) throws Exception;

  void stop(String name, String queue) throws Exception;

  void call(String name, String queue, Date timestamp, Map<String, String> params) throws Exception;

  void call(String name, String queue, Date timestamp, String jsonParams) throws Exception;

  boolean isScheduled(String name, String queue) throws Exception;

  boolean isEventDriven(String name, String queue) throws Exception;

  List<String> queues() throws Exception;

  Application get(String name, String queue) throws Exception;

  List<Application> list() throws Exception;

  List<Application> list(String queue) throws Exception;

  List<ApplicationRecord> listRunning(String queue) throws Exception;

  void addDependency(ApplicationDependency dependency) throws Exception;

  void removeDependency(String name, String queue, String dependName, String dependQueue)
      throws Exception;

  List<ApplicationDependency> getDependencies(String name, String queue) throws Exception;

  List<Message> queued() throws Exception;

  ApplicationRecord getRecord(String name, String queue, Date scheduleTime) throws Exception;

  List<ApplicationRecord> listRecords(String name, String queue, Date beginTime, Date endTime)
      throws Exception;

  List<ScheduleApplication> repair(String name, String queue, Date beginTime, Date endTime)
      throws Exception;

  List<ScheduleApplication> reply(String name, String queue, Date beginTime, Date endTime)
      throws Exception;

  boolean deleteQueued(int id) throws Exception;

  boolean kill(String name, String queue, Date scheduleTime) throws Exception;

  String log(String name, String queue, Date scheduleTime) throws Exception;

  Application createApplication();

  ApplicationDependency createApplicationDependency();

  List<ApplicationRecord> getApplicationRecordsBySate(String queue, int state) throws SQLException;
}
