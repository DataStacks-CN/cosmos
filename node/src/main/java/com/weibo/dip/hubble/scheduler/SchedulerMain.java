package com.weibo.dip.hubble.scheduler;

import com.weibo.dip.hubble.common.ClasspathProperties;
import com.weibo.dip.hubble.common.GlobalResource;
import com.weibo.dip.hubble.scheduler.common.Conf;
import com.weibo.dip.hubble.scheduler.core.AppExecutor;
import com.weibo.dip.hubble.scheduler.db.SchedulerDataSource;
import com.weibo.dip.hubble.scheduler.db.SchedulerDataSourceFactory;
import com.weibo.dip.hubble.scheduler.db.SchedulerOperator;
import com.weibo.dip.hubble.scheduler.queue.MessageQueue;
import com.weibo.dip.hubble.scheduler.service.SchedulerServiceImpl;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scheduler Main.
 *
 * @author yurun
 */
public class SchedulerMain {
  private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerMain.class);

  /**
   * Main.
   *
   * <p>Start sequence:
   *
   * <p>(1) Properties
   *
   * <p>(2) DataSource
   *
   * <p>(3) MessageQueue
   *
   * <p>(4) SchedulerOperator
   *
   * <p>(5) AppExecutor
   *
   * <p>(6) QuartzScheduler
   *
   * <p>(7) JettyServer
   *
   * <p>Stop sequence:(7) -> (1)
   *
   * @param args no params
   * @throws Exception if start/stop error
   */
  public static void main(String[] args) throws Exception {
    // classpath properties
    ClasspathProperties properties = new ClasspathProperties(Conf.SCHEDULER_PROPERTIES);
    LOGGER.info("Classpath properties {} loaded", Conf.SCHEDULER_PROPERTIES);

    // datasource
    SchedulerDataSourceFactory dataSourceFactory = new SchedulerDataSourceFactory();
    dataSourceFactory.initialize(properties);

    SchedulerDataSource dataSource = dataSourceFactory.getDataSource();

    GlobalResource.register(dataSource);
    LOGGER.info("Scheduler datasource created");

    // message queue
    MessageQueue queue = new MessageQueue(dataSource);
    LOGGER.info("Message queue created");

    SchedulerOperator operator = new SchedulerOperator(dataSource);
    LOGGER.info("Scheduler operator created");

    AppExecutor executor = new AppExecutor(properties, queue, operator);

    executor.start();
    LOGGER.info("App executor started");

    // quartz scheduler
    StdSchedulerFactory schedulerFactory = new StdSchedulerFactory();
    schedulerFactory.initialize(properties.getProperties());

    Scheduler scheduler = schedulerFactory.getScheduler();
    scheduler.start();
    LOGGER.info("Quartz scheduler started");

    // jetty server
    Server server = new Server(properties.getInt("server.port"));

    ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);

    handler.addServlet(
        new ServletHolder(new SchedulerServiceImpl(queue, scheduler, operator)),
        "/service/scheduler");

    server.setHandler(handler);

    server.start();
    LOGGER.info("Jetty server started");

    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  try {
                    server.stop();
                    LOGGER.info("Jetty server stoped");

                    scheduler.shutdown(true);
                    LOGGER.info("Quartz scheduler stoped");

                    executor.stop();
                    LOGGER.info("App executor stoped");

                    dataSource.close();
                    LOGGER.info("Scheduler datasource stoped");

                    GlobalResource.clear();
                  } catch (Exception e) {
                    LOGGER.error("Scheduler stop error: {}", ExceptionUtils.getFullStackTrace(e));
                  }
                }));
  }
}
