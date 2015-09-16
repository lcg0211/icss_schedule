package com.icss.schedule;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.DateBuilder.futureDate;

import java.util.Date;

import org.quartz.DateBuilder.IntervalUnit;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerMetaData;
import org.quartz.SimpleTrigger;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.icss.common.CommonUtil;

public class SchedMain {
//	private static final String JOB1_CRON = "JDOAuthRefreshTokenJobCron";
//	private static final String JOB2_CRON = "JDGetDeliveryNumJobCron";
//	private static final String JOB3_CRON = "JDSendDeliveryInfoJobCron";
//	private static final String JOB4_CRON = "ZTOSendDeliveryInfoJobCron";
	private static final String JOB1_INTERVAL = "JDOAuthRefreshTokenJobInterval";
	private static final String JOB2_INTERVAL = "JDGetDeliveryNumJobInterval";
	private static final String JOB3_INTERVAL = "JDSendDeliveryInfoJobInterval";
	private static final String JOB4_INTERVAL = "ZTOSendDeliveryInfoJobInterval";
	private static final String JOB1_ACTIVREFLAG = "JDOAuthRefreshTokenJobActiveflag";
	private static final String JOB2_ACTIVREFLAG = "JDGetDeliveryNumJobActiveflag";
	private static final String JOB3_ACTIVREFLAG = "JDSendDeliveryInfoJobActiveflag";
	private static final String JOB4_ACTIVREFLAG = "ZTOSendDeliveryInfoJobActiveflag";
	private static final String JOB1_Delay = "JDOAuthRefreshTokenJobDelay";
	private static final String JOB2_Delay = "JDGetDeliveryNumJobDelay";
	private static final String JOB3_Delay = "JDSendDeliveryInfoJobDelay";
	private static final String JOB4_Delay = "ZTOSendDeliveryInfoJobDelay";
	private static Logger log = LoggerFactory.getLogger(SchedMain.class);
	private static Scheduler sched;

	public static void run() throws Exception {
		String jobFlag1 = CommonUtil.getConfProperty(JOB1_ACTIVREFLAG);
		String jobFlag2 = CommonUtil.getConfProperty(JOB2_ACTIVREFLAG);
		String jobFlag3 = CommonUtil.getConfProperty(JOB3_ACTIVREFLAG);
		String jobFlag4 = CommonUtil.getConfProperty(JOB4_ACTIVREFLAG);
		int jobDelay1 = Integer
				.parseInt(CommonUtil.getConfProperty(JOB1_Delay));
		int jobDelay2 = Integer
				.parseInt(CommonUtil.getConfProperty(JOB2_Delay));
		int jobDelay3 = Integer
				.parseInt(CommonUtil.getConfProperty(JOB3_Delay));
		int jobDelay4 = Integer
				.parseInt(CommonUtil.getConfProperty(JOB4_Delay));
		int jobInterval1 = Integer.parseInt(CommonUtil
				.getConfProperty(JOB1_INTERVAL));
		int jobInterval2 = Integer.parseInt(CommonUtil
				.getConfProperty(JOB2_INTERVAL));
		int jobInterval3 = Integer.parseInt(CommonUtil
				.getConfProperty(JOB3_INTERVAL));
		int jobInterval4 = Integer.parseInt(CommonUtil
				.getConfProperty(JOB4_INTERVAL));
		 JobDetail job1 = null;
		 JobDetail job2 = null;
		 JobDetail job3 = null;
		 JobDetail job4 = null;
		 SimpleTrigger trigger1 = null;
		 SimpleTrigger trigger2 = null;
		 SimpleTrigger trigger3 = null;
		 SimpleTrigger trigger4 = null;
		Date dt = null;
		int flagCount=0;
		// refresh京东access_token
		if ("Y".equals(jobFlag1)) {
		 job1 = newJob(JDOAuthRefreshTokenJob.class).withIdentity(
				"authJob1", "group1").build();
		 trigger1 = newTrigger()
				.withIdentity("authTrigger1", "group1")
				.startAt(futureDate(jobDelay1, IntervalUnit.SECOND))
				.withSchedule(
						simpleSchedule().withIntervalInSeconds(jobInterval1)
								.repeatForever()).forJob("authJob1", "group1")
				.build();
		}
		// 预约京东物流单号
		 job2 = newJob(JDGetDeliveryNumJob.class).withIdentity(
				"expressJob1", "group1").build();
		 trigger2 = newTrigger()
				.withIdentity("expressTrigger1", "group1")
				.startAt(futureDate(jobDelay2, IntervalUnit.SECOND))
				.withSchedule(
						simpleSchedule().withIntervalInSeconds(jobInterval2)
								.repeatForever()).forJob("expressJob1", "group1")
				.build();

		// 向京东提交运单信息
		 job3 = newJob(JDSendDeliveryInfoJob.class).withIdentity(
				"expressJob2", "group1").build();
		 trigger3 = newTrigger()
				.withIdentity("expressTrigger2", "group1")
				.startAt(futureDate(jobDelay3, IntervalUnit.SECOND))
				.withSchedule(
						simpleSchedule().withIntervalInSeconds(jobInterval3)
								.repeatForever()).forJob("expressJob2", "group1")
				.build();

		// // 向中通提交运单信息
		 job4 = newJob(ZTOSendDeliveryInfoJob.class).withIdentity(
				"expressJob3", "group1").build();
		 trigger4 = newTrigger()
				.withIdentity("expressTrigger3", "group1")
				.startAt(futureDate(jobDelay4, IntervalUnit.SECOND))
				.withSchedule(
						simpleSchedule().withIntervalInSeconds(jobInterval4)
								.repeatForever()).forJob("expressJob3", "group1")
				.build();
		log.info("激活的Job数为：【" + flagCount + "】");
		 if(flagCount>0) {
		 sched = StdSchedulerFactory.getDefaultScheduler();
		 if ("Y".equals(jobFlag1)) {
		 dt=sched.scheduleJob(job1, trigger1);
		 log.info(job1.getKey().getName()+"开始时间为："+CommonUtil.formattedDate(dt));
		 }
		 if ("Y".equals(jobFlag2)) {
		 dt=sched.scheduleJob(job2, trigger2);
		 log.info(job2.getKey().getName()+"开始时间为："+CommonUtil.formattedDate(dt));
		 }
		 if ("Y".equals(jobFlag3)) {
		 dt=sched.scheduleJob(job3, trigger3);
		 log.info(job3.getKey().getName()+"开始时间为："+CommonUtil.formattedDate(dt));
		 }
		 if ("Y".equals(jobFlag4)) {
		 dt=sched.scheduleJob(job4, trigger4);
		 log.info(job4.getKey().getName()+"开始时间为："+CommonUtil.formattedDate(dt));
		 }
		 sched.start();
		 }
//		sched.start();
	}

	// 停止
	public static void stop() throws Exception {
		sched.shutdown(true);
		log.info("调度已关闭");
		SchedulerMetaData smd = sched.getMetaData();
		log.info("执行了" + smd.getNumberOfJobsExecuted() + "个jobs");
	}

	public static void main(String[] args) throws Exception {
		SchedMain.run();
	}
}