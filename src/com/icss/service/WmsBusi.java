package com.icss.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.icss.jdbc.JdbcUtils;

public final class WmsBusi {
	private static Logger log = LoggerFactory.getLogger(WmsBusi.class);

	public static int addDeliveryNum(String customerId, String shopId,
			List<String> list) {
		StringBuffer valueSql = new StringBuffer();
		StringBuffer sql = new StringBuffer();
		Iterator<String> iter = list.iterator();
		List<String> initValue = new ArrayList<String>();
		int retCount = 0;
		while (iter.hasNext()) {
			String deliveryNum = iter.next();
			initValue.addAll(getInitDevliveryInfo(customerId, deliveryNum,
					shopId));
			valueSql.append("(?,?,?,?,?,?,?,?,?,?,?),");
		}
		valueSql.deleteCharAt(valueSql.length() - 1);
		sql.append("INSERT INTO [SYS_Delivery_No]"
				+ "([CustomerID],[DeliveryNo],[UseFlag]"
				+ ",[CarrierID],[WarehouseID],[issueparty]"
				+ ",[CHANNEL],[CLIENTID],[sequenceno]"
				+ ",[pushflag],[shopid]) values ");
		sql.append(valueSql);
		Object[] obj = initValue.toArray();
		retCount = JdbcUtils.execute(String.valueOf(sql), obj);
		return retCount;
	}

	private static List<String> getInitDevliveryInfo(String cusId, String deliveryNo,
			String shopId) {
		if (cusId == null || deliveryNo == null || shopId == null
				|| "".equals(cusId) || "".equals(deliveryNo)
				|| "".equals(shopId)) {
			log.error("参数不能为空！");
			return null;
		}
		List<String> initValue = new ArrayList<String>();
		initValue.add(cusId);
		initValue.add(deliveryNo);
		initValue.add("N");
		initValue.add("*");
		initValue.add("*");
		initValue.add("*");
		initValue.add("*");
		initValue.add(shopId);
		initValue.add("");
		initValue.add("N");
		initValue.add(shopId);
		return initValue;
	}

	public static Map<String, Object> getJDAuthInfo() {
		String sql = "SELECT top 1 [app_key] as appKey,"
				+ "[app_secret] as appSecret,"
				+ "[access_token] as accessToken,"
				+ "[refresh token] as refreshToken FROM [TMP_JD_AUTH_INFO] with(nolock)";
		Map<String, Object> retMap = JdbcUtils.querySingular(sql);
		return retMap;
	}

	public static List<Map<String, Object>> getJDShop() {
		String sql = "select shop_id as shopId," + "pre_num as preNum "
				+ "from TMP_JD_SHOP with(nolock) where get_delivery_num_flag=?";
		Object[] obj = { "Y" };
		List<Map<String, Object>> retList = JdbcUtils.queryPlural(sql, obj);
		log.info("京东店铺配置信息："+retList.toString());
		return retList;
	}

	public static int modifyJDShopGettime(String shopId) {
		if (shopId == null || "".equals(shopId)) {
			log.error("参数不能为空！");
			return 0;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String curTime = sdf.format(System.currentTimeMillis());
		String sql = "update TMP_JD_SHOP set last_get_time=? where shop_id=?";
		Object[] obj = { curTime, shopId };
		int retCount = JdbcUtils.execute(sql, obj);
		return retCount;
	}

	public static List<Map<String, Object>> getSendDeliveryInfo() {
//		String sql = "select * from idx_jdexpress_getorder with(nolock)";
		String sql="SELECT [deliveryId]"
      +",[salePlat]"
      +",[customerCode]"
      +",[orderId]"
      +",[thrOrderId]"
      +",[selfPrintWayBill]"
      +",[pickMethod]"
      +",[packageRequired]"
      +",[senderName]"
      +",[senderAddress]"
      +",[senderTel]"
      +",[senderMobile]"
      +",[senderPostcode]"
      +",[receiveName]"
      +",[receiveAddress]"
      +",[province]"
      +",[city]"
      +",[county]"
      +",[town]"
      +",[provinceId]"
      +",[cityId]"
      +",[countyId]"
      +",[townId]"
      +",[siteType]"
      +",[siteId]"
      +",[siteName]"
      +",[receiveTel]"
      +",[receiveMobile]"
      +",[postcode]"
      +",[packageCount]"
      +",[weight]"
      +",[vloumLong]"
      +",[vloumWidth]"
      +",[vloumHeight]"
      +",[vloumn]"
      +",[description]"
      +",[collectionValue]"
      +",[collectionMoney]"
      +",[guaranteeValue]"
      +",[guaranteeValueAmount]"
      +",[signReturn]"
      +",[aging]"
      +",[transType]"
      +",[remark]"
      +",[goodsType]"
      +",[orderType]"
      +",[shopCode]"
      +",[orderSendTime]"
      +",[warehouseCode]"
      +",[extendField1]"
      +",[extendField2]"
      +",[extendField3]"
      +",[extendField4]"
      +",[extendField5]"
  +" FROM [idx_jdexpress_getorder]";
		List<Map<String, Object>> retList = JdbcUtils.queryPlural(sql);
		if (retList ==null ) {
		log.info("运单信息数据库查询返回为空");
		return null;
		}
		log.info("运单信息："+retList.toString());
		return retList;
	}

	public static int modifyDeliveryPushtime(String expressId, String deliveryNum) {
		if (expressId == null || "".equals(expressId) || deliveryNum == null
				|| "".equals(deliveryNum)) {
			log.error("参数不能为空！");
			return 0;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String curTime = sdf.format(System.currentTimeMillis());
		String sql = "update SYS_Delivery_No set pushflag=?,pushtime=? where customerid=? and deliveryno=? ";
		Object[] obj = { "Y", curTime,expressId,deliveryNum };
		int retCount = JdbcUtils.execute(sql, obj);
		return retCount;
	}
}
