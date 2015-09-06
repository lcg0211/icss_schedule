package com.icss.schedule;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.CronScheduleBuilder.*;

import java.util.Date;
import java.util.Properties;

import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerMetaData;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.icss.common.CommonUtil;

public class SchedMain {
	private static Logger log = LoggerFactory.getLogger(SchedMain.class);
	private static Scheduler sched;

	public static void run() throws Exception {
		Properties pt = new Properties();
		pt.load(SchedMain.class.getResourceAsStream("/../config/qconf.properties")); //路径为 WEB-INF/config/qconf.properties
		String cronSchedule1 = pt.getProperty("JDOAuthRefreshTokenJob");
		String cronSchedule2 = pt.getProperty("JDGetDeliveryNumJob");
		String cronSchedule3 = pt.getProperty("JDSendDeliveryInfoJob");
		
		//refresh京东access_token
		JobDetail job1 = newJob(JDOAuthRefreshTokenJob.class).withIdentity("authJob1",
				"group1").build();
		CronTrigger trigger1 = newTrigger()
				.withIdentity("authTrigger1", "group1")
				.withSchedule(cronSchedule(cronSchedule1))
				.forJob("authJob1", "group1").build();
		//预约京东物流单号
		JobDetail job2 = newJob(JDGetDeliveryNumJob.class).withIdentity("expressJob1",
				"group1").build();
		CronTrigger trigger2 = newTrigger()
				.withIdentity("expressTrigger1", "group1")
				.withSchedule(cronSchedule(cronSchedule2))
				.forJob("expressJob1", "group1").build();
		//向京东提交运单信息
		JobDetail job3 = newJob(JDSendDeliveryInfoJob.class).withIdentity("expressJob2",
				"group1").build();
		CronTrigger trigger3 = newTrigger()
				.withIdentity("expressTrigger2", "group1")
				.withSchedule(cronSchedule(cronSchedule3))
				.forJob("expressJob2", "group1").build();
		sched = StdSchedulerFactory.getDefaultScheduler();
		Date dt=sched.scheduleJob(job1, trigger1);
		log.info(job1.getKey().getName()+"开始时间为："+CommonUtil.FormattedDate(dt));
		dt=sched.scheduleJob(job2, trigger2);
		log.info(job2.getKey().getName()+"开始时间为："+CommonUtil.FormattedDate(dt));
		dt=sched.scheduleJob(job3, trigger3);
		log.info(job3.getKey().getName()+"开始时间为："+CommonUtil.FormattedDate(dt));
		sched.start();
	}


	// 停止
	public static void stop() throws Exception {
		sched.shutdown(true);
		log.info("调度已关闭");
		SchedulerMetaData smd = sched.getMetaData();
		log.info("执行了"+smd.getNumberOfJobsExecuted()+"个jobs");
	}

	public static void main(String[] args) throws Exception {
		SchedMain.run();
	}
}