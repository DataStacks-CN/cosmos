package com.weibo.dip.cosmos.node.quartz;

import com.weibo.dip.cosmos.model.Application;
import com.weibo.dip.cosmos.model.ApplicationRecord;
import com.weibo.dip.cosmos.model.ApplicationState;
import com.weibo.dip.cosmos.model.ScheduleApplication;
import com.weibo.dip.cosmos.node.db.SchedulerDataSource;
import com.weibo.dip.cosmos.node.db.SchedulerOperator;
import com.weibo.dip.cosmos.node.queue.MessageQueue;
import com.weibo.dip.durian.GlobalResource;
import com.weibo.dip.durian.util.GsonUtil;
import com.weibo.dip.durian.util.IpUtil;
import java.util.Date;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Quartz job.
 *
 * @author yurun
 */
public class QuartzJob implements Job {
  private static final Logger LOGGER = LoggerFactory.getLogger(QuartzJob.class);

  public static final String NAME = "name";
  public static final String QUEUE = "queue";

  public static final long SCHEDULE_DELAY = 3 * 1000;

  private SchedulerDataSource dataSource = GlobalResource.get(SchedulerDataSource.class);

  private MessageQueue queue = new MessageQueue(dataSource);
  private SchedulerOperator operator = new SchedulerOperator(dataSource);

  @Override
  public void execute(JobExecutionContext context) {
    Date scheduleTime = context.getScheduledFireTime();
    Date executeTime = context.getFireTime();

    if (executeTime.getTime() - scheduleTime.getTime() >= SCHEDULE_DELAY) {
      LOGGER.warn("Quartz node is busy, schedule delay: {}", SCHEDULE_DELAY);
    }

    String applicationName = context.getMergedJobDataMap().getString(NAME);
    String applicationQueue = context.getMergedJobDataMap().getString(QUEUE);

    try {
      Application application = operator.getApplication(applicationName, applicationQueue);

      ApplicationRecord applicationRecord =
          new ApplicationRecord(
              application.getName(),
              application.getQueue(),
              IpUtil.getLocalhost(),
              scheduleTime,
              executeTime,
              ApplicationState.QUEUED);

      ScheduleApplication scheduleApplication =
          new ScheduleApplication(application, applicationRecord);

      queue.produce(
          GsonUtil.toJson(scheduleApplication),
          scheduleApplication.getQueue(),
          scheduleApplication.getPriority(),
          scheduleTime);
      operator.addOrUpdateApplicationRecord(applicationRecord);

      LOGGER.info("Application {} queued", scheduleApplication.getUniqeName());
    } catch (Exception e) {
      LOGGER.error(
          "Application {}_{} queue error: {}",
          applicationName,
          applicationQueue,
          ExceptionUtils.getFullStackTrace(e));
    }
  }
}
