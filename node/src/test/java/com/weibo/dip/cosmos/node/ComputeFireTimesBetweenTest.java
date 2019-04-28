package com.weibo.dip.cosmos.node;

import org.apache.commons.lang3.time.DateUtils;
import org.quartz.CronScheduleBuilder;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerUtils;
import org.quartz.spi.OperableTrigger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created By zhiqiang32 on 2019-04-28
 */
public class ComputeFireTimesBetweenTest {

    public static void main(String[] args) throws ParseException {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date scheduleTime =  simpleDateFormat.parse("2019-04-28 16:10:00");

        int fromSeconds = 230700;
        int toSeconds = 144300;

        String dependedCron = "0 10 * * * ?";

        List<Date> fireTimes =
                TriggerUtils.computeFireTimesBetween(
                        (OperableTrigger)
                                TriggerBuilder.newTrigger()
                                        .withSchedule(CronScheduleBuilder.cronSchedule(dependedCron))
                                        .build(),
                        null,
                        DateUtils.addSeconds(scheduleTime, -fromSeconds), // include
                        DateUtils.addSeconds(scheduleTime, -toSeconds)); // include

        System.out.println(fireTimes.size());
        fireTimes.forEach(System.out::println);
    }

}
