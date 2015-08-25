package com.icss.schedule;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class SchedMainListerner implements ServletContextListener{
	//初始化监听器，创建实例，执行任务
	public void contextInitialized(ServletContextEvent event){ 
		try {
			event.getServletContext().log("------监听器已启动------") ;  
			SchedMain.run();
			event.getServletContext().log("------调度程序已启动------") ;  
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//销毁监听器，停止执行任务
	public void contextDestroyed(ServletContextEvent event){ 
		try {
			SchedMain.stop();
			event.getServletContext().log("------监听器已停止------") ;  
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
