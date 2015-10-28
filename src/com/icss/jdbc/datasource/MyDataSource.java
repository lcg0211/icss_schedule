package com.icss.jdbc.datasource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.icss.common.CommonUtil;

public class MyDataSource {
	// private static String url = "jdbc:mysql://localhost:3306/jdbc";
	// private static String url = "jdbc:sqlserver://192.168.1.4;"
	// + "databaseName=WMS;";
	private String dbUrl = null;
	private String dbUser = null;
	private String dbPasswd = null;
	private int connPoolInitCount = 0;// 初始化最大连接数
	private int connPoolMaxCount = 0; // 最大连接数限制
	private int currentCount = 0;
	private LinkedList<Connection> connectionsPool = new LinkedList<Connection>();
	private static Logger log = LoggerFactory.getLogger(MyDataSource.class);

	public MyDataSource(String Url, String User, String Passwd, int initCount,
			int maxCount) {
		this.dbUrl = Url;
		this.dbUser = User;
		this.dbPasswd = Passwd;
		this.connPoolInitCount = initCount;
		this.connPoolMaxCount = maxCount;
		for (int i = 0; i < connPoolInitCount; i++) {
			try {
				this.connectionsPool.addLast(this.createConnection());
				this.currentCount++;
			} catch (SQLException e) {
				throw new ExceptionInInitializerError(e);
			}
		}
		log.info("---数据库连接池创建成功，当前池中空闲连接数为【" + this.connectionsPool.size()
				+ "】，全局连接数为【" + currentCount + "】---" + CommonUtil.curDate());
	}

	public Connection getConnection() throws SQLException {
		Connection conn = null;
		synchronized (connectionsPool) {
			if (this.connectionsPool.size() > 0) {
				conn = this.connectionsPool.removeFirst();
				log.info("---从连接池中取连接成功，当前池中空闲连接数为【"
						+ this.connectionsPool.size() + "】---"
						+ CommonUtil.curDate());
				return conn;
			}
			if (this.currentCount < connPoolMaxCount) {
				log.info("---连接池中已没有空闲连接，需要重新创建新连接...---"
						+ CommonUtil.curDate());
				conn = this.createConnection();
				this.currentCount++;
				log.info("---当前全局连接数为【" + currentCount + "】---"
						+ CommonUtil.curDate());
				return conn;
			}
			throw new SQLException("数据库连接数已经达到最大限制");
		}
	}

	public void free(Connection conn) {
		this.connectionsPool.addLast(conn);
		log.info("---已将数据库连接放回池中，当前池中空闲连接数为【" + this.connectionsPool.size()
				+ "】---" + CommonUtil.curDate());
	}

	private Connection createConnection() throws SQLException {
		Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPasswd);
		log.info("---数据库创建新连接成功---" + CommonUtil.curDate());
		return conn;
	}

}
