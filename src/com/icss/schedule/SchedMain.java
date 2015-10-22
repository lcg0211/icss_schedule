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
	// private static final String JOB1_CRON = "JDOAuthRefreshTokenJobCron";
	// private static final String JOB2_CRON = "JDGetDeliveryNumJobCron";
	// private static final String JOB3_CRON = "JDSendDeliveryInfoJobCron";
	// private static final String JOB4_CRON = "ZTOSendDeliveryInfoJobCron";
	private static final String JOB1_INTERVAL = "JDOAuthRefreshTokenJobInterval";
	private static final String JOB2_INTERVAL = "JDGetDeliveryNumJobInterval";
	private static final String JOB3_INTERVAL = "JDSendDeliveryInfoJobInterval";
	private static final String JOB4_INTERVAL = "ZTOSendDeliveryInfoJobInterval";
	private static final String JOB5_INTERVAL = "ZTOGetMarkBySingleJobInterval";
	private static final String JOB6_INTERVAL = "JDGetOrderInfoJobInterval";
	private static final String JOB7_INTERVAL = "ZTOGetDeliveryNumJobInterval";
	private static final String JOB1_ACTIVREFLAG = "JDOAuthRefreshTokenJobActiveflag";
	private static final String JOB2_ACTIVREFLAG = "JDGetDeliveryNumJobActiveflag";
	private static final String JOB3_ACTIVREFLAG = "JDSendDeliveryInfoJobActiveflag";
	private static final String JOB4_ACTIVREFLAG = "ZTOSendDeliveryInfoJobActiveflag";
	private static final String JOB5_ACTIVREFLAG = "ZTOGetMarkBySingleJobActiveflag";
	private static final String JOB6_ACTIVREFLAG = "JDGetOrderInfoJobActiveflag";
	private static final String JOB7_ACTIVREFLAG = "ZTOGetDeliveryNumJobActiveflag";
	private static final String JOB1_Delay = "JDOAuthRefreshTokenJobDelay";
	private static final String JOB2_Delay = "JDGetDeliveryNumJobDelay";
	private static final String JOB3_Delay = "JDSendDeliveryInfoJobDelay";
	private static final String JOB4_Delay = "ZTOSendDeliveryInfoJobDelay";
	private static final String JOB5_Delay = "ZTOGetMarkBySingleJobDelay";
	private static final String JOB6_Delay = "JDGetOrderInfoJobDelay";
	private static final String JOB7_Delay ="ZTOGetDeliveryNumJobDelay";
	private static Logger log = LoggerFactory.getLogger(SchedMain.class);
	private static Scheduler sched;

	public static void run() throws Exception {
		String jobFlag1 = CommonUtil.getConfProperty(JOB1_ACTIVREFLAG);
		String jobFlag2 = CommonUtil.getConfProperty(JOB2_ACTIVREFLAG);
		String jobFlag3 = CommonUtil.getConfProperty(JOB3_ACTIVREFLAG);
		String jobFlag4 = CommonUtil.getConfProperty(JOB4_ACTIVREFLAG);
		String jobFlag5 = CommonUtil.getConfProperty(JOB5_ACTIVREFLAG);
		String jobFlag6 = CommonUtil.getConfProperty(JOB6_ACTIVREFLAG);
		String jobFlag7 = CommonUtil.getConfProperty(JOB7_ACTIVREFLAG);
		int jobDelay1 = Integer
				.parseInt(CommonUtil.getConfProperty(JOB1_Delay));
		int jobDelay2 = Integer
				.parseInt(CommonUtil.getConfProperty(JOB2_Delay));
		int jobDelay3 = Integer
				.parseInt(CommonUtil.getConfProperty(JOB3_Delay));
		int jobDelay4 = Integer
				.parseInt(CommonUtil.getConfProperty(JOB4_Delay));
		int jobDelay5 = Integer
				.parseInt(CommonUtil.getConfProperty(JOB5_Delay));
		int jobDelay6 = Integer
				.parseInt(CommonUtil.getConfProperty(JOB6_Delay));
		int jobDelay7 = Integer
				.parseInt(CommonUtil.getConfProperty(JOB7_Delay));
		int jobInterval1 = Integer.parseInt(CommonUtil
				.getConfProperty(JOB1_INTERVAL));
		int jobInterval2 = Integer.parseInt(CommonUtil
				.getConfProperty(JOB2_INTERVAL));
		int jobInterval3 = Integer.parseInt(CommonUtil
				.getConfProperty(JOB3_INTERVAL));
		int jobInterval4 = Integer.parseInt(CommonUtil
				.getConfProperty(JOB4_INTERVAL));
		int jobInterval5 = Integer.parseInt(CommonUtil
				.getConfProperty(JOB5_INTERVAL));
		int jobInterval6 = Integer.parseInt(CommonUtil
				.getConfProperty(JOB6_INTERVAL));
		int jobInterval7 = Integer.parseInt(CommonUtil
				.getConfProperty(JOB7_INTERVAL));
		JobDetail job1 = null;
		JobDetail job2 = null;
		JobDetail job3 = null;
		JobDetail job4 = null;
		JobDetail job5 = null;
		JobDetail job6 = null;
		JobDetail job7 = null;
		SimpleTrigger trigger1 = null;
		SimpleTrigger trigger2 = null;
		SimpleTrigger trigger3 = null;
		SimpleTrigger trigger4 = null;
		SimpleTrigger trigger5 = null;
		SimpleTrigger trigger6 = null;
		SimpleTrigger trigger7 = null;
		Date dt = null;
		int flagCount = 0;
		// 1. refresh京东access_token
		if ("Y".equals(jobFlag1)) {
			job1 = newJob(JDOAuthRefreshTokenJob.class).withIdentity(
					"authJob1", "group1").build();
			trigger1 = newTrigger()
					.withIdentity("authTrigger1", "group1")
					.startAt(futureDate(jobDelay1, IntervalUnit.SECOND))
					.withSchedule(
							simpleSchedule()
									.withIntervalInSeconds(jobInterval1)
									.repeatForever())
					.forJob("authJob1", "group1").build();
			flagCount++;
		}
		// 2 . 预约京东物流单号
		if ("Y".equals(jobFlag2)) {
			job2 = newJob(JDGetDeliveryNumJob.class).withIdentity(
					"expressJob1", "group1").build();
			trigger2 = newTrigger()
					.withIdentity("expressTrigger1", "group1")
					.startAt(futureDate(jobDelay2, IntervalUnit.SECOND))
					.withSchedule(
							simpleSchedule()
									.withIntervalInSeconds(jobInterval2)
									.repeatForever())
					.forJob("expressJob1", "group1").build();
			flagCount++;
		}
		// 3.向京东提交运单信息
		if ("Y".equals(jobFlag3)) {
			job3 = newJob(JDSendDeliveryInfoJob.class).withIdentity(
					"expressJob2", "group1").build();
			trigger3 = newTrigger()
					.withIdentity("expressTrigger2", "group1")
					.startAt(futureDate(jobDelay3, IntervalUnit.SECOND))
					.withSchedule(
							simpleSchedule()
									.withIntervalInSeconds(jobInterval3)
									.repeatForever())
					.forJob("expressJob2", "group1").build();
			flagCount++;
		}
		// 4.向中通提交运单信息
		if ("Y".equals(jobFlag4)) {
			job4 = newJob(ZTOSendDeliveryInfoJob.class).withIdentity(
					"expressJob3", "group1").build();
			trigger4 = newTrigger()
					.withIdentity("expressTrigger3", "group1")
					.startAt(futureDate(jobDelay4, IntervalUnit.SECOND))
					.withSchedule(
							simpleSchedule()
									.withIntervalInSeconds(jobInterval4)
									.repeatForever())
					.forJob("expressJob3", "group1").build();
			flagCount++;
		}
		// 5.向中通获取大头笔(单笔)
		if ("Y".equals(jobFlag5)) {
			job5 = newJob(ZTOGetMarkBySingleJob.class).withIdentity(
					"expressJob4", "group1").build();
			trigger5 = newTrigger()
					.withIdentity("expressTrigger4", "group1")
					.startAt(futureDate(jobDelay5, IntervalUnit.SECOND))
					.withSchedule(
							simpleSchedule()
									.withIntervalInSeconds(jobInterval5)
									.repeatForever())
					.forJob("expressJob4", "group1").build();
			flagCount++;
		}
		// 6. 获取京东订单信息
		if ("Y".equals(jobFlag6)) {
			job6 = newJob(JDGetOrderInfoJob.class).withIdentity(
					"expressJob5", "group1").build();
			trigger6 = newTrigger()
					.withIdentity("expressTrigger5", "group1")
					.startAt(futureDate(jobDelay6, IntervalUnit.SECOND))
					.withSchedule(
							simpleSchedule()
									.withIntervalInSeconds(jobInterval6)
									.repeatForever())
					.forJob("expressJob5", "group1").build();
			flagCount++;
		}
		// 7. 获取中通电子面单号
		if ("Y".equals(jobFlag7)) {
			job7 = newJob(ZTOGetDeliveryNumJob.class).withIdentity(
					"expressJob6", "group1").build();
			trigger7 = newTrigger()
					.withIdentity("expressTrigger6", "group1")
					.startAt(futureDate(jobDelay7, IntervalUnit.SECOND))
					.withSchedule(
							simpleSchedule()
									.withIntervalInSeconds(jobInterval7)
									.repeatForever())
					.forJob("expressJob6", "group1").build();
			flagCount++;
		}
		log.info("激活的Job数为：【" + flagCount + "】");
		if (flagCount > 0) {
			sched = StdSchedulerFactory.getDefaultScheduler();
			if ("Y".equals(jobFlag1)) {
				dt = sched.scheduleJob(job1, trigger1);
				log.info(job1.getKey().getName() + "开始时间为："
						+ CommonUtil.formattedDate(dt));
			}
			if ("Y".equals(jobFlag2)) {
				dt = sched.scheduleJob(job2, trigger2);
				log.info(job2.getKey().getName() + "开始时间为："
						+ CommonUtil.formattedDate(dt));
			}
			if ("Y".equals(jobFlag3)) {
				dt = sched.scheduleJob(job3, trigger3);
				log.info(job3.getKey().getName() + "开始时间为："
						+ CommonUtil.formattedDate(dt));
			}
			if ("Y".equals(jobFlag4)) {
				dt = sched.scheduleJob(job4, trigger4);
				log.info(job4.getKey().getName() + "开始时间为："
						+ CommonUtil.formattedDate(dt));
			}
			if ("Y".equals(jobFlag5)) {
				dt = sched.scheduleJob(job5, trigger5);
				log.info(job5.getKey().getName() + "开始时间为："
						+ CommonUtil.formattedDate(dt));
			}
			if ("Y".equals(jobFlag6)) {
				dt = sched.scheduleJob(job6, trigger6);
				log.info(job6.getKey().getName() + "开始时间为："
						+ CommonUtil.formattedDate(dt));
			}
			if ("Y".equals(jobFlag7)) {
				dt = sched.scheduleJob(job7, trigger7);
				log.info(job7.getKey().getName() + "开始时间为："
						+ CommonUtil.formattedDate(dt));
			}
			sched.start();
		}
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