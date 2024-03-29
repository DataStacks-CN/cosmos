package com.weibo.dip.cosmos.model;

import com.weibo.dip.durian.Symbols;
import com.weibo.dip.durian.util.DatetimeUtil;
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

  @Override
  public String getUniqeName() {
    return super.getUniqeName()
        + Symbols.LINE_THROUGH
        + DatetimeUtil.DATETIME_FORMAT.format(applicationRecord.getScheduleTime());
  }
}
