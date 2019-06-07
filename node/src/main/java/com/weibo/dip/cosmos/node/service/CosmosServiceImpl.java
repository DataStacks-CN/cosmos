package com.weibo.dip.cosmos.node.service;

import com.caucho.hessian.server.HessianServlet;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DockerClientBuilder;
import com.google.common.base.Preconditions;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.CharEncoding;
import org.apache.commons.lang.StringUtils;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.TriggerUtils;
import org.quartz.impl.matchers.GroupMatcher;
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

  private boolean scheduled(String name, String queue) throws SchedulerException {
    return scheduler.checkExists(new JobKey(name, queue));
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
        application.getName(), application.getQueue());

    operator.addApplication(application);

    LOGGER.info(
        "Application {}:{} added: {}",
        application.getName(), application.getQueue(),
        application);
  }

  @Override
  public void update(Application application) throws Exception {
    Preconditions.checkState(Objects.nonNull(application), "Application must be specifiled");

    Preconditions.checkState(
        operator.existApplication(application.getName(), application.getQueue()),
        "Application %s:%s not exist",
        application.getName(), application.getQueue());
    Preconditions.checkState(!scheduled(application.getName(), application.getQueue()),
        "Application %s:%s still handles the scheduling state", application.getName(),
        application.getQueue());

    operator.updateApplication(application);
    LOGGER.info(
        "Application {}:{} updated: {}",
        application.getName(), application.getQueue(),
        application);
  }

  @Override
  public void delete(String name, String queue) throws Exception {
    Preconditions.checkState(
        StringUtils.isNotEmpty(name) && StringUtils.isNotEmpty(queue),
        "name and queue must be specified");

    Preconditions.checkState(!scheduled(name, queue),
        "Application %s:%s still handles the scheduling state", name, queue);

    operator.deleteApplication(name, queue);

    LOGGER.info("Application {}:{} deleted", name, queue);
  }

  @Override
  public void start(Application application) throws Exception {
    Preconditions.checkState(Objects.nonNull(application), "Application must be specifiled");

    Preconditions.checkState(
        !operator.existApplication(application.getName(), application.getQueue()),
        "Application %s already started",
        application.getUniqeName());

    // create jobdetail
    JobKey jobKey = new JobKey(application.getName(), application.getQueue());

    Preconditions.checkState(
        !scheduler.checkExists(jobKey),
        "Application %s does not in db, but exist in node",
        application.getUniqeName());

    JobDataMap data = new JobDataMap();

    data.put(QuartzJob.NAME, application.getName());
    data.put(QuartzJob.QUEUE, application.getQueue());

    JobDetail job =
        JobBuilder.newJob(QuartzJob.class).withIdentity(jobKey).usingJobData(data).build();

    // create trigger
    TriggerKey triggerKey = new TriggerKey(application.getName(), application.getQueue());

    Trigger trigger =
        TriggerBuilder.newTrigger()
            .withIdentity(triggerKey)
            .withSchedule(
                CronScheduleBuilder.cronSchedule(application.getCron())
                    .withMisfireHandlingInstructionDoNothing())
            .build();

    // schedule and add
    scheduler.scheduleJob(job, trigger);

    operator.addApplication(application);

    LOGGER.info(
        "Application {}:{} started: {}",
        application.getName(),
        application.getQueue(),
        application.getCron());
  }

  @Override
  public void stop(String name, String queue) throws Exception {
    Preconditions.checkState(
        StringUtils.isNotEmpty(name) && StringUtils.isNotEmpty(queue),
        "name and queue must be specified");

    Preconditions.checkState(
        operator.existApplication(name, queue), "Application %s_%s does not exist", name, queue);

    JobKey jobKey = new JobKey(name, queue);
    TriggerKey triggerKey = new TriggerKey(name, queue);

    scheduler.unscheduleJob(triggerKey);
    scheduler.deleteJob(jobKey);

    operator.deleteApplication(name, queue);

    LOGGER.info("Application {}:{} stoped", name, queue);
  }

  @Override
  public Application get(String name, String queue) throws Exception {
    return operator.getApplication(name, queue);
  }

  @Override
  public List<String> queues() throws Exception {
    return scheduler.getJobGroupNames();
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
    Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.groupEquals(queue));
    if (CollectionUtils.isEmpty(jobKeys)) {
      return null;
    }

    List<Application> applications = new ArrayList<>();

    for (JobKey jobKey : jobKeys) {
      applications.add(operator.getApplication(jobKey.getName(), jobKey.getGroup()));
    }

    return applications;
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

  @Override
  public List<ScheduleApplication> repair(String name, String queue, Date beginTime, Date endTime)
      throws Exception {
    JobKey jobKey = new JobKey(name, queue);

    Preconditions.checkState(
        scheduler.checkExists(jobKey), "Application %s:%s does not exist", name, queue);

    Application application = operator.getApplication(name, queue);

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
      ApplicationRecord record = operator.getApplicationRecord(name, queue, fireTime);

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
  public List<ScheduleApplication> reply(String name, String queue, Date beginTime, Date endTime)
      throws Exception {
    JobKey jobKey = new JobKey(name, queue);

    Preconditions.checkState(
        scheduler.checkExists(jobKey), "Application %s:%s does not exist", name, queue);

    Application application = operator.getApplication(name, queue);

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
        || !(record.getState().equals(ApplicationState.SUCCESS)
        || record.getState().equals(ApplicationState.FAILED)
        || record.getState().equals(ApplicationState.KILLED))) {
      return null;
    }

    // container log
    String containerLog =
        properties.getString("docker.container.log")
            + Symbols.SLASH
            + record.getName()
            + Symbols.SLASH
            + DatetimeUtil.DATETIME_FORMAT.format(record.getScheduleTime());

    StringBuilder logs = new StringBuilder();

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

    return logs.toString();
  }
}
