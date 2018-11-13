package com.weibo.dip.hubble.scheduler.bean;

import com.weibo.dip.hubble.common.Symbols;
import com.weibo.dip.hubble.common.util.DatetimeUtil;
import java.io.Serializable;

/**
 * Schedule application.
 *
 * @author yurun
 */
public class ScheduleApplication extends Application implements Serializable {
  private ApplicationRecord applicationRecord;

  /**
   * Construct a instance.
   *
   * @param application application instance
   * @param applicationRecord application record
   */
  public ScheduleApplication(Application application, ApplicationRecord applicationRecord) {
    super(
        application.getName(),
        application.getQueue(),
        application.getUser(),
        application.getPriority(),
        application.getCores(),
        application.getMems(),
        application.getRepository(),
        application.getTag(),
        application.getParams(),
        application.getCron(),
        application.getTimeout());

    this.applicationRecord = applicationRecord;
  }

  public ApplicationRecord getApplicationRecord() {
    return applicationRecord;
  }

  public void setApplicationRecord(ApplicationRecord applicationRecord) {
    this.applicationRecord = applicationRecord;
  }

  @Override
  public String getUniqeName() {
    return super.getUniqeName()
        + Symbols.UNDERLINE
        + DatetimeUtil.DATETIME_FORMAT.format(applicationRecord.getScheduleTime());
  }
}
