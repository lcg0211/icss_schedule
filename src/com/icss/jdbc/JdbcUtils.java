package com.icss.jdbc;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.icss.jdbc.datasource.MyDataSource;

public final class JdbcUtils {
	private static MyDataSource myDataSource = null;

	private JdbcUtils() {
	}

	static {
		try {
			Properties pt = new Properties();
			pt.load(JdbcUtils.class.getResourceAsStream("/../config/jdbc.properties")); //路径为 WEB-INF/config/jdbc.properties
			String classname = pt.getProperty("jdbc.driverClassName");
			String dbUrl = pt.getProperty("jdbc.url");
			String dbUser = pt.getProperty("jdbc.username");
			String dbPasswd = pt.getProperty("jdbc.password");
			System.out.println("【"+classname+"】【"+dbUrl+"】【"+dbUser+"】");
			Class.forName(classname);
			myDataSource = new MyDataSource(dbUrl, dbUser, dbPasswd);
		} catch (ClassNotFoundException e) {
			throw new ExceptionInInitializerError(e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Connection getConnection() throws SQLException {
		return myDataSource.getConnection();
	}

	public static void free(ResultSet rs, Statement st, Connection conn) {
		try {
			if (rs != null)
				rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (st != null)
					st.close();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (conn != null)
					try {
						// conn.close();
						myDataSource.free(conn);
					} catch (Exception e) {
						e.printStackTrace();
					}
			}
		}
	}

	@SuppressWarnings("finally")
	public static int queryCount(String sql, Object[] params) {
		if (sql.trim() == "" || params.length == 0)
			return -1;
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		int retCount = 0;
		Object obj = null;
		try {
			conn = JdbcUtils.getConnection();
			ps = conn.prepareStatement(sql);
			for (int i = 1; i <= params.length; i++) {
				obj = params[i - 1];
				ps.setObject(i, obj);
			}
			rs = ps.executeQuery();
			if (rs.next())
				retCount = rs.getInt(1);
		} catch (SQLException e) {
			e.printStackTrace();
			retCount = -1;
		} finally {
			JdbcUtils.free(rs, ps, conn);
			return retCount;
		}
	}

	// 普通语句执行
	@SuppressWarnings("finally")
	public static int execute(String sql, Object[] params) {
		if (sql.trim() == "" || params.length == 0)
			return -1;
		Connection conn = null;
		PreparedStatement ps = null;
		int retCount = 0;
		Object obj = null;
		ResultSet rs = null;
		try {
			conn = JdbcUtils.getConnection();
			ps = conn.prepareStatement(sql);
			for (int i = 1; i <= params.length; i++) {
				obj = params[i - 1];
				if (obj == null)
					ps.setString(i, "");
				if (obj instanceof String)
					ps.setString(i, (String) obj);
				else if (obj instanceof Boolean)
					ps.setBoolean(i, (Boolean) obj);
				else if (obj instanceof Date)
					ps.setDate(i, (Date) obj);
				else if (obj instanceof Double)
					ps.setDouble(i, (Double) obj);
				else if (obj instanceof Float)
					ps.setFloat(i, (Float) obj);
				else if (obj instanceof Integer)
					ps.setInt(i, (Integer) obj);
				else if (obj instanceof Long)
					ps.setLong(i, (Long) obj);
				else if (obj instanceof Short)
					ps.setShort(i, (Short) obj);
				else if (obj instanceof Time)
					ps.setTime(i, (Time) obj);
				else if (obj instanceof Timestamp)
					ps.setTimestamp(i, (Timestamp) obj);
				else
					ps.setObject(i, obj);
				// System.out.println(obj.toString());
			}
			retCount = ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			retCount = 0;
		} finally {
			JdbcUtils.free(rs, ps, conn);
			return retCount;
		}
	}

	// 运行事务
	@SuppressWarnings("finally")
	public static int executeTX(String[] sqls, Object[][] params) {
		if (sqls.length == 0 || params.length == 0)
			return -1;
		Connection conn = null;
		PreparedStatement ps = null;
		int retCount = 0;
		Object obj = null;
		ResultSet rs = null;
		try {
			conn = JdbcUtils.getConnection();
			conn.setAutoCommit(false);
			for (int i = 1; i <= sqls.length; i++) { // 遍历sqls数组
				ps = conn.prepareStatement(sqls[i - 1]);
				for (int j = 1; j <= params[i - 1].length; j++) { // 遍历当前sql对应的object数组
					obj = params[i - 1][j - 1];
					if (obj == null)
						ps.setString(j, "");
					else
						ps.setObject(j, obj);
				}
				retCount = ps.executeUpdate();
			}
			conn.commit();
		} catch (SQLException e) {
			if (conn != null)
				conn.rollback();
			e.printStackTrace();
			retCount = 0;
		} finally {
			JdbcUtils.free(rs, ps, conn);
			return retCount;
		}
	}

	// 批量查询
	@SuppressWarnings("finally")
	public static List<Map<String, Object>> queryPlural(String sql,
			Object[] params) {
		if (sql.trim() == "" || params.length == 0)
			return null;
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Object obj = null;
		List<Map<String, Object>> datas = null;
		try {
			conn = JdbcUtils.getConnection();
			ps = conn.prepareStatement(sql);
			for (int i = 1; i <= params.length; i++) {
				obj = params[i - 1];
				ps.setObject(i, obj);
			}
			rs = ps.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			int count = rsmd.getColumnCount();
			String[] colNames = new String[count];
			for (int i = 1; i <= count; i++) {
				colNames[i - 1] = rsmd.getColumnLabel(i);
			}
			datas = new ArrayList<Map<String, Object>>();
			while (rs.next()) { // 循环取多行
				Map<String, Object> data = new HashMap<String, Object>();
				for (int i = 0; i < colNames.length; i++) {
					data.put(colNames[i], rs.getObject(colNames[i]));
				}
				datas.add(data);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			JdbcUtils.free(rs, ps, conn);
			return datas;
		}
	}
	
	// 批量查询
		@SuppressWarnings("finally")
		public static List<Map<String, Object>> queryPlural(String sql) {
			if (sql.trim() == "")
				return null;
			Connection conn = null;
			PreparedStatement ps = null;
			Statement st=null;
			ResultSet rs = null;
//			Object obj = null;
			List<Map<String, Object>> datas = null;
			try {
				conn = JdbcUtils.getConnection();
				st=conn.createStatement();
//				ps = conn.prepareStatement(sql);
//				for (int i = 1; i <= params.length; i++) {
//					obj = params[i - 1];
//					ps.setObject(i, obj);
//				}
//				rs = ps.executeQuery();
				rs=st.executeQuery(sql);
				ResultSetMetaData rsmd = rs.getMetaData();
				int count = rsmd.getColumnCount();
				String[] colNames = new String[count];
				for (int i = 1; i <= count; i++) {
					colNames[i - 1] = rsmd.getColumnLabel(i);
				}
				datas = new ArrayList<Map<String, Object>>();
				while (rs.next()) { // 循环取多行
					Map<String, Object> data = new HashMap<String, Object>();
					for (int i = 0; i < colNames.length; i++) {
						System.out.println(String.valueOf(colNames[i]));
						data.put(colNames[i], rs.getObject(colNames[i]));
					}
					datas.add(data);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				JdbcUtils.free(rs, ps, conn);
				return datas;
			}
		}

	// 单个查询
	@SuppressWarnings("finally")
	public static Map<String, Object> querySingular(String sql, Object[] params) {
		if (sql.trim() == "" || params.length == 0)
			return null;
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Object obj = null;
		Map<String, Object> data = new HashMap<String, Object>();
		try {
			conn = JdbcUtils.getConnection();
			ps = conn.prepareStatement(sql);
			for (int i = 1; i <= params.length; i++) {
				obj = params[i - 1];
				ps.setObject(i, obj);
			}
			rs = ps.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			int count = rsmd.getColumnCount();
			String[] colNames = new String[count];
			for (int i = 1; i <= count; i++) {
				colNames[i - 1] = rsmd.getColumnLabel(i);
			}
			if (rs.next()) { // 只取第一行
				for (int i = 0; i < colNames.length; i++) {
					data.put(colNames[i], rs.getObject(colNames[i]));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			JdbcUtils.free(rs, ps, conn);
			return data;
		}
	}
	
	// 单个查询
		@SuppressWarnings("finally")
		public static Map<String, Object> querySingular(String sql) {
			if (sql.trim() == "")
				return null;
			Connection conn = null;
			Statement st=null;
			PreparedStatement ps = null;
			ResultSet rs = null;
			Map<String, Object> data = new HashMap<String, Object>();
			try {
				conn = JdbcUtils.getConnection();
//				ps = conn.prepareStatement(sql);
				st=conn.createStatement();
				rs=st.executeQuery(sql);
//				for (int i = 1; i <= params.length; i++) {
//					obj = params[i - 1];
//					ps.setObject(i, obj);
//				}
//				rs = ps.executeQuery();
				ResultSetMetaData rsmd = rs.getMetaData();
				int count = rsmd.getColumnCount();
				String[] colNames = new String[count];
				for (int i = 1; i <= count; i++) {
					colNames[i - 1] = rsmd.getColumnLabel(i);
				}
				if (rs.next()) { // 只取第一行
					for (int i = 0; i < colNames.length; i++) {
						data.put(colNames[i], rs.getObject(colNames[i]));
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				JdbcUtils.free(rs, ps, conn);
				return data;
			}
		}

	// 调用SP
	@SuppressWarnings("finally")
	public static String callSP(String sql, Object[] params, int outParaCount) {
		if (sql.trim() == "" || params.length == 0 || outParaCount == 0)
			return null;
		Connection conn = null;
		CallableStatement cs = null;
		ResultSet rs = null;
		Object obj = null;
		String retStr = null;
		try {
			conn = JdbcUtils.getConnection();
			cs = conn.prepareCall(sql);
			for (int j = 1; j <= outParaCount; j++) {
				cs.registerOutParameter(params.length + j,
						java.sql.Types.VARCHAR);
			}
			for (int i = 1; i <= params.length; i++) {
				obj = params[i - 1];
				cs.setObject(i, obj);
			}
			cs.execute();
			// cs.executeUpdate();
			retStr = cs.getString(params.length + outParaCount); // 输出参数的最后一个参数作为返回字符串
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			JdbcUtils.free(rs, cs, conn);
			return retStr;
		}

	}
}
