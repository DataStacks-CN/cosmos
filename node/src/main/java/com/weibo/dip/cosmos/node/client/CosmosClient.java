package com.weibo.dip.cosmos.node.client;

import com.caucho.hessian.client.HessianProxyFactory;
import com.weibo.dip.cosmos.model.Application;
import com.weibo.dip.cosmos.model.ApplicationDependency;
import com.weibo.dip.cosmos.model.ApplicationRecord;
import com.weibo.dip.cosmos.model.Message;
import com.weibo.dip.cosmos.model.ScheduleApplication;
import com.weibo.dip.cosmos.service.CosmosService;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/** Scheduler service. */
public class CosmosClient implements CosmosService {
  private CosmosService schedulerService;

  /**
   * Construct a node service instance.
   *
   * @param host node server hostname
   * @param port node server port
   * @throws Exception if create proxy error
   */
  public CosmosClient(String host, int port) throws Exception {
    HessianProxyFactory proxyFactory = new HessianProxyFactory();

    proxyFactory.setOverloadEnabled(true);

    schedulerService =
        (CosmosService)
            proxyFactory.create(
                CosmosService.class, "http://" + host + ":" + port + "/service/node");
  }

  @Override
  public boolean connect() {
    return schedulerService.connect();
  }

  @Override
  public void add(Application application) throws Exception {
    schedulerService.add(application);
  }

  @Override
  public void update(Application application) throws Exception {
    schedulerService.update(application);
  }

  @Override
  public void delete(String name, String queue) throws Exception {
    schedulerService.delete(name, queue);
  }

  @Override
  public void start(String name, String queue) throws Exception {
    schedulerService.start(name, queue);
  }

  @Override
  public void stop(String name, String queue) throws Exception {
    schedulerService.stop(name, queue);
  }

  @Override
  public void call(String name, String queue, Date timestamp, Map<String, String> params)
      throws Exception {
    schedulerService.call(name, queue, timestamp, params);
  }

  @Override
  public void call(String name, String queue, Date timestamp, String jsonParams) throws Exception {
    schedulerService.call(name, queue, timestamp, jsonParams);
  }

  @Override
  public boolean isScheduled(String name, String queue) throws Exception {
    return schedulerService.isScheduled(name, queue);
  }

  @Override
  public boolean isEventDriven(String name, String queue) throws Exception {
    return schedulerService.isEventDriven(name, queue);
  }

  @Override
  public Application get(String name, String queue) throws Exception {
    return schedulerService.get(name, queue);
  }

  @Override
  public List<String> queues() throws Exception {
    return schedulerService.queues();
  }

  @Override
  public List<Application> list() throws Exception {
    return schedulerService.list();
  }

  @Override
  public List<Application> list(String queue) throws Exception {
    return schedulerService.list(queue);
  }

  @Override
  public List<ApplicationRecord> listRunning(String queue) throws Exception {
    return schedulerService.listRunning(queue);
  }

  @Override
  public void addDependency(ApplicationDependency dependency) throws Exception {
    schedulerService.addDependency(dependency);
  }

  @Override
  public void removeDependency(String name, String queue, String dependName, String dependQueue)
      throws Exception {
    schedulerService.removeDependency(name, queue, dependName, dependQueue);
  }

  @Override
  public List<ApplicationDependency> getDependencies(String name, String queue) throws Exception {
    return schedulerService.getDependencies(name, queue);
  }

  @Override
  public List<Message> queued() throws Exception {
    return schedulerService.queued();
  }

  @Override
  public ApplicationRecord getRecord(String name, String queue, Date scheduleTime)
      throws Exception {
    return schedulerService.getRecord(name, queue, scheduleTime);
  }

  @Override
  public List<ApplicationRecord> listRecords(
      String name, String queue, Date beginTime, Date endTime) throws Exception {
    return schedulerService.listRecords(name, queue, beginTime, endTime);
  }

  @Override
  public List<ScheduleApplication> repair(String name, String queue, Date beginTime, Date endTime)
      throws Exception {
    return schedulerService.repair(name, queue, beginTime, endTime);
  }

  @Override
  public List<ScheduleApplication> reply(String name, String queue, Date beginTime, Date endTime)
      throws Exception {
    return schedulerService.reply(name, queue, beginTime, endTime);
  }

  @Override
  public boolean deleteQueued(int id) throws Exception {
    return schedulerService.deleteQueued(id);
  }

  @Override
  public boolean kill(String name, String queue, Date scheduleTime) throws Exception {
    return schedulerService.kill(name, queue, scheduleTime);
  }

  @Override
  public String log(String name, String queue, Date scheduleTime) throws Exception {
    return schedulerService.log(name, queue, scheduleTime);
  }

  @Override
  public Application createApplication() {
    return schedulerService.createApplication();
  }

  @Override
  public ApplicationDependency createApplicationDependency() {
    return schedulerService.createApplicationDependency();
  }

  @Override
  public List<ApplicationRecord> getApplicationRecordsBySate(String queue, int state)
      throws SQLException {
    return schedulerService.getApplicationRecordsBySate(queue, state);
  }
}
