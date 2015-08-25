package com.icss.jdbc.datasource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;

public class MyDataSource {
	// private static String url = "jdbc:mysql://localhost:3306/jdbc";
	// private static String url = "jdbc:sqlserver://192.168.1.4;"
	// + "databaseName=WMS;";
	private String dbUrl = null;
	private String dbUser = null;
	private String dbPasswd = null;
	private static int initCount = 20; // 启动时初始化连接数
	private static int maxCount = 100; // 最大连接数限制
	private int currentCount = 0;
	private LinkedList<Connection> connectionsPool = new LinkedList<Connection>();

	public MyDataSource(String Url, String User,
			String Passwd) {
		this.dbUrl=Url;
		this.dbUser=User;
		this.dbPasswd=Passwd;
		for (int i = 0; i < initCount; i++) {
			try {
				this.connectionsPool.addLast(this.createConnection());
				this.currentCount++;
			} catch (SQLException e) {
				throw new ExceptionInInitializerError(e);
			}
		}
		System.out.println("数据库连接池创建成功");
	}

	public Connection getConnection() throws SQLException {
		Connection conn = null;
		synchronized (connectionsPool) {
			if (this.connectionsPool.size() > 0) {
				conn = this.connectionsPool.removeFirst();
				System.out.println("从连接池中取连接成功");
				return conn;
			}
			if (this.currentCount < maxCount) {
				this.currentCount++;
				System.out.println("连接池中已没有空闲连接，需要重新创建新连接...");
				conn = this.createConnection();
				return conn;
			}
			throw new SQLException("数据库连接数已经达到最大限制");
		}
	}

	public void free(Connection conn) {
		this.connectionsPool.addLast(conn);
		System.out.println("已将数据库连接放回池中");
	}

	private Connection createConnection() throws SQLException {
		Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPasswd);
		System.out.println("数据库创建新连接成功");
		return conn;
	}

}
