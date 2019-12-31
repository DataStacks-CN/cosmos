package com.weibo.dip.cosmos.node.service;

import com.caucho.hessian.server.HessianServlet;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DockerClientBuilder;
import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.weibo.dip.cosmos.model.Application;
import com.weibo.dip.cosmos.model.ApplicationDependency;
import com.weibo.dip.cosmos.model.ApplicationRecord;
import com.weibo.dip.cosmos.model.ApplicationState;
import com.weibo.dip.cosmos.model.Message;
import com.weibo.dip.cosmos.model.ScheduleApplication;
import com.weibo.dip.cosmos.node.common.Conf;
import com.weibo.dip.cosmos.node.db.SchedulerOperator;
import com.weibo.dip.cosmos.node.quartz.QuartzJob;
import com.weibo.dip.cosmos.node.queue.MessageQueue;
import com.weibo.dip.cosmos.service.CosmosService;
import com.weibo.dip.durian.ClasspathProperties;
import com.weibo.dip.durian.Symbols;
import com.weibo.dip.durian.util.DatetimeUtil;
import com.weibo.dip.durian.util.GsonUtil;
import com.weibo.dip.durian.util.IpUtil;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.CharEncoding;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.TriggerUtils;
import org.quartz.spi.OperableTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scheduler Service Impl.
 *
 * @author yurun
 */
public class CosmosServiceImpl extends HessianServlet implements CosmosService {
  private static final Logger LOGGER = LoggerFactory.getLogger(CosmosServiceImpl.class);

  private ClasspathProperties properties;
  private MessageQueue queue;
  private Scheduler scheduler;
  private SchedulerOperator operator;

  private JsonParser parser = new JsonParser();

  /**
   * Construct a instance.
   *
   * @param properties ClasspathProperties
   * @param queue MessageQueue
   * @param scheduler Scheduler
   * @param operator SchedulerOperator
   */
  public CosmosServiceImpl(
      ClasspathProperties properties,
      MessageQueue queue,
      Scheduler scheduler,
      SchedulerOperator operator) {
    this.properties = properties;
    this.queue = queue;
    this.scheduler = scheduler;
    this.operator = operator;
  }

  @Override
  public boolean connect() {
    return true;
  }

  @Override
  public void add(Application application) throws Exception {
    Preconditions.checkState(Objects.nonNull(application), "Application must be specifiled");

    Preconditions.checkState(
        !operator.existApplication(application.getName(), application.getQueue()),
        "Application %s:%s already existed",
        application.getName(),
        application.getQueue());

    operator.addApplication(application);

    LOGGER.info(
        "Application {}:{} added: {}", application.getName(), application.getQueue(), application);
  }

  @Override
  public void update(Application application) throws Exception {
    Preconditions.checkState(Objects.nonNull(application), "Application must be specifiled");

    Preconditions.checkState(
        operator.existApplication(application.getName(), application.getQueue()),
        "Application %s:%s not exist",
        application.getName(),
        application.getQueue());
    Preconditions.checkState(
        !isScheduled(application.getName(), application.getQueue()),
        "Application %s:%s still handles the scheduling state",
        application.getName(),
        application.getQueue());

    operator.updateApplication(application);
    LOGGER.info(
        "Application {}:{} updated: {}",
        application.getName(),
        application.getQueue(),
        application);
  }

  @Override
  public void delete(String name, String queue) throws Exception {
    Preconditions.checkState(
        StringUtils.isNotEmpty(name) && StringUtils.isNotEmpty(queue),
        "name and queue must be specified");

    Preconditions.checkState(
        !isScheduled(name, queue),
        "Application %s:%s still handles the scheduling state",
        name,
        queue);

    operator.deleteApplication(name, queue);

    LOGGER.info("Application {}:{} deleted", name, queue);
  }

  @Override
  public void start(String name, String queue) throws Exception {
    Preconditions.checkState(
        StringUtils.isNotEmpty(name) && StringUtils.isNotEmpty(queue),
        "name and queue must be specified");

    Application application = get(name, queue);

    Preconditions.checkState(
        Objects.nonNull(application), "Application %s:%s not exist", name, queue);
    Preconditions.checkState(
        !isEventDriven(name, queue), "Application %s:%s unsupport start operation", name, queue);
    Preconditions.checkState(
        !isScheduled(name, queue), "Application %s:%s already started", name, queue);

    JobDataMap data = new JobDataMap();

    data.put(QuartzJob.NAME, name);
    data.put(QuartzJob.QUEUE, queue);

    JobDetail job =
        JobBuilder.newJob(QuartzJob.class)
            .withIdentity(new JobKey(name, queue))
            .usingJobData(data)
            .build();

    Trigger trigger =
        TriggerBuilder.newTrigger()
            .withIdentity(new TriggerKey(name, queue))
            .withSchedule(
                CronScheduleBuilder.cronSchedule(application.getCron())
                    .withMisfireHandlingInstructionDoNothing())
            .build();

    // schedule
    scheduler.scheduleJob(job, trigger);

    LOGGER.info("Application {}:{} started", application.getName(), application.getQueue());
  }

  @Override
  public void stop(String name, String queue) throws Exception {
    Preconditions.checkState(
        StringUtils.isNotEmpty(name) && StringUtils.isNotEmpty(queue),
        "name and queue must be specified");

    Preconditions.checkState(
        operator.existApplication(name, queue), "Application %s:%s not exist", name, queue);
    Preconditions.checkState(
        !isEventDriven(name, queue), "Application %s:%s unsupport stop operation", name, queue);

    JobKey jobKey = new JobKey(name, queue);
    TriggerKey triggerKey = new TriggerKey(name, queue);

    scheduler.unscheduleJob(triggerKey);
    scheduler.deleteJob(jobKey);

    LOGGER.info("Application {}:{} stoped", name, queue);
  }

  @Override
  public void call(String name, String queue, Date timestamp, Map<String, String> params)
      throws Exception {
    Preconditions.checkState(
        StringUtils.isNotEmpty(name) && StringUtils.isNotEmpty(queue) && Objects.nonNull(timestamp),
        "name, queue and timestamp must be specified");

    Application application = get(name, queue);

    Preconditions.checkState(
        Objects.nonNull(application), "Application %s:%s not exist", name, queue);
    Preconditions.checkState(
        isEventDriven(name, queue), "Application %s:%s unsupport call operation", name, queue);

    if (MapUtils.isNotEmpty(params)) {
      JsonObject jsonParams = parser.parse(application.getParams()).getAsJsonObject();

      params.forEach(jsonParams::addProperty);

      application.setParams(jsonParams.toString());
    }

    Date now = new Date();

    ApplicationRecord applicationRecord =
        new ApplicationRecord(
            application.getName(),
            application.getQueue(),
            IpUtil.getLocalhost(),
            timestamp,
            now,
            ApplicationState.QUEUED);

    ScheduleApplication scheduleApplication =
        new ScheduleApplication(application, applicationRecord);

    this.queue.produce(
        GsonUtil.toJson(scheduleApplication),
        scheduleApplication.getQueue(),
        scheduleApplication.getPriority(),
        now);
    operator.addOrUpdateApplicationRecord(applicationRecord);

    LOGGER.info("Application {} called", scheduleApplication.getUniqeName());
  }

  @Override
  public boolean isScheduled(String name, String queue) throws Exception {
    return !isEventDriven(name, queue) && scheduler.checkExists(new JobKey(name, queue));
  }

  @Override
  public boolean isEventDriven(String name, String queue) throws Exception {
    Application application = get(name, queue);

    return Objects.nonNull(application) && Application.EVENT_DRIVEN.equals(application.getCron());
  }

  @Override
  public Application get(String name, String queue) throws Exception {
    return operator.getApplication(name, queue);
  }

  @Override
  public List<String> queues() throws Exception {
    return operator.getQueues();
  }

  @Override
  public List<Application> list() throws Exception {
    List<String> queues = queues();
    if (CollectionUtils.isEmpty(queues)) {
      return null;
    }

    List<Application> applications = new ArrayList<>();

    for (String queue : queues) {
      List<Application> apps = list(queue);
      if (CollectionUtils.isNotEmpty(apps)) {
        applications.addAll(apps);
      }
    }

    return applications;
  }

  @Override
  public List<Application> list(String queue) throws Exception {
    return operator.getApplications(queue);
  }

  @Override
  public List<ApplicationRecord> listRunning(String queue) throws Exception {
    return operator.getRunningApplicationRecords(queue);
  }

  @Override
  public void addDependency(ApplicationDependency dependency) throws Exception {
    Preconditions.checkState(
        Objects.nonNull(dependency), "Application dependency must be specified");
    Preconditions.checkState(
        operator.existApplication(dependency.getName(), dependency.getQueue()),
        "Application %s_%s does not exist",
        dependency.getName(),
        dependency.getQueue());
    Preconditions.checkState(
        operator.existApplication(dependency.getDependName(), dependency.getDependQueue()),
        "Depend application %s_%s does not exist",
        dependency.getDependName(),
        dependency.getDependQueue());

    operator.addOrUpdateApplicationDependency(dependency);

    LOGGER.info(
        "Application {}_{} add dependency {}_{} with [{}, {}]",
        dependency.getName(),
        dependency.getQueue(),
        dependency.getDependName(),
        dependency.getDependQueue(),
        dependency.getFromSeconds(),
        dependency.getToSeconds());
  }

  @Override
  public void removeDependency(String name, String queue, String dependName, String dependQueue)
      throws Exception {
    Preconditions.checkState(
        operator.existApplication(name, queue), "Application %s_%s does not exist", name, queue);
    Preconditions.checkState(
        operator.existApplication(dependName, dependQueue),
        "Depend application %s_%s does not exist",
        name,
        queue);

    operator.deleteApplicationDependency(name, queue, dependName, dependQueue);

    LOGGER.info("Application {}_{} remove dependency {}_{}", name, queue, dependName, dependQueue);
  }

  @Override
  public List<ApplicationDependency> getDependencies(String name, String queue) throws Exception {
    return operator.getDependencies(name, queue);
  }

  @Override
  public List<Message> queued() throws Exception {
    return queue.listQueued();
  }

  @Override
  public ApplicationRecord getRecord(String name, String queue, Date scheduleTime)
      throws Exception {
    return operator.getApplicationRecord(name, queue, scheduleTime);
  }

  @Override
  public List<ApplicationRecord> listRecords(
      String name, String queue, Date beginTime, Date endTime) throws Exception {
    return operator.getApplicationRecords(name, queue, beginTime, endTime);
  }

  private List<ScheduleApplication> repairEventDrivenApplication(
      Application application, Date beginTime, Date endTime) throws Exception {
    List<ApplicationRecord> records =
        listRecords(application.getName(), application.getQueue(), beginTime, endTime);
    if (CollectionUtils.isEmpty(records)) {
      return null;
    }

    List<ScheduleApplication> scheduleApplications = new ArrayList<>();

    Date now = new Date();

    for (ApplicationRecord record : records) {
      if (record.getState().equals(ApplicationState.FAILED)
          || record.getState().equals(ApplicationState.KILLED)) {
        ApplicationRecord applicationRecord =
            new ApplicationRecord(
                record.getName(),
                record.getQueue(),
                IpUtil.getLocalhost(),
                record.getScheduleTime(),
                now,
                ApplicationState.QUEUED);

        ScheduleApplication scheduleApplication =
            new ScheduleApplication(application, applicationRecord);

        scheduleApplications.add(scheduleApplication);

        this.queue.produce(
            GsonUtil.toJson(scheduleApplication),
            scheduleApplication.getQueue(),
            scheduleApplication.getPriority(),
            now);
        operator.addOrUpdateApplicationRecord(applicationRecord);

        LOGGER.info("Application {} repaired", scheduleApplication.getUniqeName());
      }
    }

    return scheduleApplications;
  }

  private List<ScheduleApplication> repairCronApplication(
      Application application, Date beginTime, Date endTime) throws Exception {
    List<Date> fireTimes =
        TriggerUtils.computeFireTimesBetween(
            (OperableTrigger)
                TriggerBuilder.newTrigger()
                    .withSchedule(CronScheduleBuilder.cronSchedule(application.getCron()))
                    .build(),
            null,
            beginTime, // include
            endTime); // include

    if (CollectionUtils.isEmpty(fireTimes)) {
      return null;
    }

    List<ScheduleApplication> scheduleApplications = new ArrayList<>();

    Date now = new Date();

    for (Date fireTime : fireTimes) {
      ApplicationRecord record =
          operator.getApplicationRecord(application.getName(), application.getQueue(), fireTime);

      if (Objects.isNull(record)
          || record.getState().equals(ApplicationState.FAILED)
          || record.getState().equals(ApplicationState.KILLED)) {
        ApplicationRecord applicationRecord =
            new ApplicationRecord(
                application.getName(),
                application.getQueue(),
                IpUtil.getLocalhost(),
                fireTime,
                now,
                ApplicationState.QUEUED);

        ScheduleApplication scheduleApplication =
            new ScheduleApplication(application, applicationRecord);

        scheduleApplications.add(scheduleApplication);

        this.queue.produce(
            GsonUtil.toJson(scheduleApplication),
            scheduleApplication.getQueue(),
            scheduleApplication.getPriority(),
            now);
        operator.addOrUpdateApplicationRecord(applicationRecord);

        LOGGER.info("Application {} repaired", scheduleApplication.getUniqeName());
      }
    }

    return scheduleApplications;
  }

  @Override
  public List<ScheduleApplication> repair(String name, String queue, Date beginTime, Date endTime)
      throws Exception {
    Application application = get(name, queue);

    Preconditions.checkState(
        Objects.nonNull(application) && scheduler.checkExists(new JobKey(name, queue)),
        "Application %s:%s does not exist",
        name,
        queue);

    return isEventDriven(name, queue)
        ? repairEventDrivenApplication(application, beginTime, endTime)
        : repairCronApplication(application, beginTime, endTime);
  }

  private List<ScheduleApplication> replayEventDrivenApplication(
      Application application, Date beginTime, Date endTime) throws Exception {
    List<ApplicationRecord> records =
        listRecords(application.getName(), application.getQueue(), beginTime, endTime);
    if (CollectionUtils.isEmpty(records)) {
      return null;
    }

    List<ScheduleApplication> scheduleApplications = new ArrayList<>();

    Date now = new Date();

    for (ApplicationRecord record : records) {
      ApplicationRecord applicationRecord =
          new ApplicationRecord(
              record.getName(),
              record.getQueue(),
              IpUtil.getLocalhost(),
              record.getScheduleTime(),
              now,
              ApplicationState.QUEUED);

      ScheduleApplication scheduleApplication =
          new ScheduleApplication(application, applicationRecord);

      scheduleApplications.add(scheduleApplication);

      this.queue.produce(
          GsonUtil.toJson(scheduleApplication),
          scheduleApplication.getQueue(),
          scheduleApplication.getPriority(),
          now);
      operator.addOrUpdateApplicationRecord(applicationRecord);

      LOGGER.info("Application {} repaired", scheduleApplication.getUniqeName());
    }

    return scheduleApplications;
  }

  private List<ScheduleApplication> replayCronApplication(
      Application application, Date beginTime, Date endTime) throws Exception {
    List<Date> fireTimes =
        TriggerUtils.computeFireTimesBetween(
            (OperableTrigger)
                TriggerBuilder.newTrigger()
                    .withSchedule(CronScheduleBuilder.cronSchedule(application.getCron()))
                    .build(),
            null,
            beginTime, // include
            endTime); // include

    if (CollectionUtils.isEmpty(fireTimes)) {
      return null;
    }

    List<ScheduleApplication> scheduleApplications = new ArrayList<>();

    Date now = new Date();

    for (Date fireTime : fireTimes) {
      ApplicationRecord applicationRecord =
          new ApplicationRecord(
              application.getName(),
              application.getQueue(),
              IpUtil.getLocalhost(),
              fireTime,
              now,
              ApplicationState.QUEUED);

      ScheduleApplication scheduleApplication =
          new ScheduleApplication(application, applicationRecord);

      scheduleApplications.add(scheduleApplication);

      this.queue.produce(
          GsonUtil.toJson(scheduleApplication),
          scheduleApplication.getQueue(),
          scheduleApplication.getPriority(),
          now);
      operator.addOrUpdateApplicationRecord(applicationRecord);

      LOGGER.info("Application {} replayed", scheduleApplication.getUniqeName());
    }

    return scheduleApplications;
  }

  @Override
  public List<ScheduleApplication> reply(String name, String queue, Date beginTime, Date endTime)
      throws Exception {
    Application application = get(name, queue);

    Preconditions.checkState(
        Objects.nonNull(application) && scheduler.checkExists(new JobKey(name, queue)),
        "Application %s:%s does not exist",
        name,
        queue);

    return isEventDriven(name, queue)
        ? replayEventDrivenApplication(application, beginTime, endTime)
        : replayCronApplication(application, beginTime, endTime);
  }

  @Override
  public boolean deleteQueued(int id) throws Exception {
    Message message = queue.get(id);
    if (Objects.isNull(message)) {
      return false;
    }

    boolean flag = false;

    if (queue.deleteQueued(id)) {
      ScheduleApplication scheduleApplication =
          GsonUtil.fromJson(message.getMessage(), ScheduleApplication.class);

      ApplicationRecord record = scheduleApplication.getApplicationRecord();
      record.setState(ApplicationState.KILLED);

      operator.addOrUpdateApplicationRecord(record);

      flag = true;
    }

    return flag;
  }

  @Override
  public boolean kill(String name, String queue, Date scheduleTime) throws Exception {
    ApplicationRecord record = operator.getApplicationRecord(name, queue, scheduleTime);
    if (Objects.isNull(record) || !record.getState().equals(ApplicationState.RUNNING)) {
      return false;
    }

    String containerName = record.getUniqeName();

    try (DockerClient dockerClient = DockerClientBuilder.getInstance().build()) {
      List<Container> containers =
          dockerClient
              .listContainersCmd()
              .withNameFilter(Collections.singleton(containerName))
              .exec();
      if (CollectionUtils.isEmpty(containers)) {
        return false;
      }

      dockerClient.killContainerCmd(containers.get(0).getId()).exec();
    }

    return true;
  }

  @Override
  public String log(String name, String queue, Date scheduleTime) throws Exception {
    ApplicationRecord record = operator.getApplicationRecord(name, queue, scheduleTime);
    if (Objects.isNull(record)
        || record.getState().equals(ApplicationState.QUEUED)
        || record.getState().equals(ApplicationState.PENDING)) {
      return "";
    }

    // container log
    String containerLog =
        properties.getString("docker.container.log")
            + Symbols.SLASH
            + record.getQueue()
            + Symbols.SLASH
            + record.getName()
            + Symbols.SLASH
            + DatetimeUtil.DATETIME_FORMAT.format(record.getScheduleTime());

    StringBuilder logs = new StringBuilder();

    try {
      logs.append(Conf.LOG_START).append(Symbols.NEWLINE);

      List<String> lines =
          FileUtils.readLines(new File(containerLog, Conf.LOG_START), CharEncoding.UTF_8);
      if (CollectionUtils.isNotEmpty(lines)) {
        lines.forEach(line -> logs.append(line).append(Symbols.NEWLINE));
      }

      logs.append(Conf.LOG_CONTAINER).append(Symbols.NEWLINE);

      lines = FileUtils.readLines(new File(containerLog, Conf.LOG_CONTAINER), CharEncoding.UTF_8);
      if (CollectionUtils.isNotEmpty(lines)) {
        lines.forEach(line -> logs.append(line).append(Symbols.NEWLINE));
      }
    } catch (FileNotFoundException e) {

      LOGGER.error(ExceptionUtils.getFullStackTrace(e));
      return "Log file is not exist!";
    }

    return logs.toString();
  }

  @Override
  public Application createApplication() {
    return new Application();
  }

  @Override
  public ApplicationDependency createApplicationDependency() {
    return new ApplicationDependency();
  }

  @Override
  public List<ApplicationRecord> getApplicationRecordsBySate(String queue, int state)
      throws SQLException {
    return operator.getApplicationRecordsBySate(queue, state);
  }
}
