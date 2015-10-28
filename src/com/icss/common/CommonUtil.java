package com.icss.common;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public final class CommonUtil {
	private static Properties pt = null;

	public static String curDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(System.currentTimeMillis());
	}

	public static String formattedDate(Date dt) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(dt);
	}

	private static Properties getPropertiesInstance() {
		if (pt == null) {
			synchronized (Properties.class) {
				if (pt == null) {
					pt = new Properties();
				}
			}
		}
		return pt;
	}

	/**
	 * @param key
	 * @param confPath
	 * @return
	 * @description 根据路径和节点名称得到property文件中的节点对应的值
	 * @author zikc
	 * @date 2015年10月28日 下午12:27:56
	 * @update 2015年10月28日 下午12:27:56
	 * @version V1.0
	 */
	public static String getConfProperty(String key, String confPath) {
		Properties prop = getPropertiesInstance();
		try {
			prop.load(CommonUtil.class.getResourceAsStream(confPath));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return prop.getProperty(key);
	}

	@SuppressWarnings("finally")
	public static String map2Json(Map<String, Object> map) {
		if (map == null | map.size() == 0)
			return "传入参数有误";
		String retStr = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			retStr = mapper.writeValueAsString(map);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			return retStr;
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked", "finally" })
	public static Map json2Map(String json) {
		if (json == null | json.length() == 0)
			return null;
		Map<String, Object> map = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			map = mapper.readValue(json, Map.class);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			return map;
		}
	}
}
