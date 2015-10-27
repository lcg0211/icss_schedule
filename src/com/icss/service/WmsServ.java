package com.icss.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.icss.common.CommonUtil;
import com.icss.jdbc.JdbcUtils;

public final class WmsServ {
	private static Logger log = LoggerFactory.getLogger(WmsServ.class);

	/**
	 * @param expressId
	 * @param shopId
	 * @param list
	 * @return
	 * @description 将运单号插入运单号表中
	 * @author zikc
	 * @date 2015年10月19日 下午4:59:33
	 * @update 2015年10月19日 下午4:59:33
	 * @version V1.0
	 */
	public static int addDeliveryNum(String expressId, String shopId,
			List<String> list) {
		if (expressId == null || shopId == null || list == null
				|| "".equals(expressId) || "".equals(shopId) || list.size()==0) {
			log.error("参数不能为空！");
			return 0;
		}
		StringBuffer valueSql = new StringBuffer();
		StringBuffer sql = new StringBuffer();
		Iterator<String> iter = list.iterator();
		List<String> initValue = new ArrayList<String>();
		int retCount = 0;
		String deliveryNum = null;
		while (iter.hasNext()) {
			deliveryNum = iter.next();
			initValue.addAll(getInitDevliveryInfo(expressId, deliveryNum,
					shopId));
			valueSql.append("(?,?,?,?,?,?,?,?,?,?,?,?),");
		}
		valueSql.deleteCharAt(valueSql.length() - 1);
		sql.append("INSERT INTO [SYS_Delivery_No]"
				+ "([CustomerID],[DeliveryNo],[UseFlag]"
				+ ",[CarrierID],[WarehouseID],[issueparty]"
				+ ",[CHANNEL],[CLIENTID],[sequenceno]"
				+ ",[pushflag],[shopid],[add_time]) values ");
		sql.append(valueSql);
		Object[] obj = initValue.toArray();
		retCount = JdbcUtils.execute(String.valueOf(sql), obj);
		return retCount;
	}
	
	public static int addDeliveryNumZTO(String expressId, String shopId,
			List<Long> list) {
		if (expressId == null || shopId == null || list == null
				|| "".equals(expressId) || "".equals(shopId) || list.size()==0) {
			log.error("参数不能为空！");
			return 0;
		}
		Iterator<Long> iter = list.iterator();
		String deliveryNum=null;
		List<String> deliveryNumList=new ArrayList<String>();
		while(iter.hasNext()) {
			deliveryNum=String.valueOf(iter.next());
			deliveryNumList.add(deliveryNum);
		}
		return addDeliveryNum(expressId,shopId,deliveryNumList);
	}

	/**
	 * @param expressId
	 * @param deliveryNo
	 * @param shopId
	 * @return
	 * @description 得到插入运单号表的初始值
	 * @author zikc
	 * @date 2015年10月19日 下午4:56:32
	 * @update 2015年10月19日 下午4:56:32
	 * @version V1.0
	 */
	private static List<String> getInitDevliveryInfo(String expressId,
			String deliveryNo, String shopId) {
		if (expressId == null || deliveryNo == null || shopId == null
				|| "".equals(expressId) || "".equals(deliveryNo)
				|| "".equals(shopId)) {
			log.error("参数不能为空！");
			return null;
		}
		List<String> initValue = new ArrayList<String>();
		initValue.add(expressId);
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
		initValue.add(CommonUtil.curDate()); // 添加时间
		return initValue;
	}

	/**
	 * @return
	 * @description 得到京东认证信息
	 * @author zikc
	 * @date 2015年10月19日 下午4:51:41
	 * @update 2015年10月19日 下午4:51:41
	 * @version V1.0
	 */
	public static Map<String, Object> getJDAuthInfo() {
		String sql = "SELECT top 1 app_key as appKey,"
				+ "app_secret as appSecret "
				+ "FROM TMP_JD_AUTH_INFO with(nolock)";
		Map<String, Object> retMap = JdbcUtils.querySingular(sql);
		return retMap;
	}

	/**
	 * @return
	 * @description 得到中通合作方信息及电子面单的配置信息
	 * @author zikc
	 * @date 2015年10月19日 下午3:38:52
	 * @update 2015年10月19日 下午3:38:52
	 * @version V1.0
	 */
	public static Map<String, Object> getZTOPartnerInfo() {
		String sql = "SELECT top 1 partner as partner," + "pass as pass,"
				+ "send_city as sendCity," + "send_address as sendAddress,"
				+ "number as number," + "last_no as lastNo,"
				+ "last_get_time as lastGetTime," + "up_limit as upLimit"
				+ " FROM TMP_ZTO_PARTNER_INFO with(nolock)";
		Map<String, Object> retMap = JdbcUtils.querySingular(sql);
		return retMap;
	}

	/**
	 * @return
	 * @description 得到京东店铺信息
	 * @author zikc
	 * @date 2015年10月19日 上午10:46:01
	 * @update 2015年10月19日 上午10:46:01
	 * @version V1.0
	 */
	public static List<Map<String, Object>> getJDShop() {
		String sql = "select shop_id as shopId," + "pre_num as preNum,"
				+ "up_limit as upLimit," + "access_token as accessToken "
				+ "from TMP_JD_SHOP with(nolock) where get_delivery_num_flag=?";
		Object[] obj = { "Y" };
		List<Map<String, Object>> retList = JdbcUtils.queryPlural(sql, obj);
		if (retList == null) {
			log.info("京东店铺信息数据库查询返回为空");
			return null;
		}
		log.info("京东店铺信息：" + retList.toString());
		return retList;
	}

	/**
	 * @param shopId
	 * @return
	 * @description 得到京东店铺的可用面单号的数量
	 * @author zikc
	 * @date 2015年10月8日 下午3:04:23
	 * @update 2015年10月8日 下午3:04:23
	 * @version V1.0
	 */
	public static int getJDAvailableDeliveryNumCount(String shopId) {
		if (shopId == null || "".equals(shopId)) {
			log.error("参数不能为空！");
			return 0;
		}
		String sql = "select count(1) from SYS_Delivery_No "
				+ "where CustomerID='JD' " + "and UseFlag='N' "
				+ "and shopid=?";
		Object[] obj = { shopId };
		int retCount = JdbcUtils.queryCount(sql, obj);
		return retCount;
	}
	
	/**
	 * @param expressId
	 * @return
	 * @description 得到快递公司的可用面单号的数量
	 * @author zikc
	 * @date 2015年10月22日 下午3:23:46
	 * @update 2015年10月22日 下午3:23:46
	 * @version V1.0
	 */
	public static int getExpressAvailableDeliveryNumCount(String expressId) {
		if (expressId == null || "".equals(expressId)) {
			log.error("参数不能为空！");
			return 0;
		}
		String sql = "select count(1) from SYS_Delivery_No "
				+ "where UseFlag='N' and CustomerID=?";
		Object[] obj = { expressId };
		int retCount = JdbcUtils.queryCount(sql, obj);
		return retCount;
	}

	/**
	 * @param shopId
	 * @return
	 * @description 更新京东店铺的最近一次电子面单的获取时间
	 * @author zikc
	 * @date 2015年10月19日 下午4:50:39
	 * @update 2015年10月19日 下午4:50:39
	 * @version V1.0
	 */
	public static int updateJDShopGettime(String shopId) {
		if (shopId == null || "".equals(shopId)) {
			log.error("参数不能为空！");
			return 0;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String curTime = sdf.format(System.currentTimeMillis());
		String sql = "update TMP_JD_SHOP set last_get_time=? where shop_id=?";
		Object[] obj = { curTime, shopId };
		log.info("【" + curTime + " | " + shopId + "】");
		int retCount = JdbcUtils.execute(sql, obj);
		return retCount;
	}
	
	/**
	 * @param partner
	 * @param lastno
	 * @return
	 * @description 更新中通合作伙伴表的最近一次电子面单的获取时间，已申请过的最后一个运单号码
	 * @author zikc
	 * @date 2015年10月19日 下午5:06:14
	 * @update 2015年10月19日 下午5:06:14
	 * @version V1.0
	 */
	public static int updateZTOGettime(String partner,String lastno) {
		if (partner == null || "".equals(partner) || lastno == null || "".equals(lastno)) {
			log.error("参数不能为空！");
			return 0;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String curTime = sdf.format(System.currentTimeMillis());
		String sql = "update TMP_ZTO_PARTNER_INFO set last_get_time=?,last_no=? where partner=?";
		Object[] obj = { curTime, lastno,partner };
		log.info("更新字段为：【" + curTime + " | " +lastno+" | "+ partner + "】");
		int retCount = JdbcUtils.execute(sql, obj);
		return retCount;
	}

	/**
	 * @param expressId
	 * @return
	 * @description 得到需要推送给物流公司的运单信息
	 * @author zikc
	 * @date 2015年10月19日 下午4:50:09
	 * @update 2015年10月19日 下午4:50:09
	 * @version V1.0
	 */
	public static List<Map<String, Object>> getSendDeliveryInfo(String expressId) {
		// String sql = "select * from idx_jdexpress_getorder with(nolock)";
		String sql = null;
		switch (expressId) {
		case "JD":
			sql = "SELECT * FROM idx_jdexpress_getorder";
			break;
		case "ZTO":
			sql = "select * from idx_ztoexpress_getorder";
			break;
		default:
			log.error("参数不符合规范");
			return null;
		}
		List<Map<String, Object>> retList = JdbcUtils.queryPlural(sql);
		if (retList == null) {
			log.info("运单信息数据库查询返回为空");
			return null;
		}
		log.info("运单信息：" + retList.toString());
		return retList;
	}

	/**
	 * @param expressId
	 * @return
	 * @description 得到需要更新字段的订单信息
	 * @author zikc
	 * @date 2015年10月19日 下午4:49:47
	 * @update 2015年10月19日 下午4:49:47
	 * @version V1.0
	 */
	public static List<Map<String, Object>> getOrderInfo(String expressId) {
		// String sql = "select * from idx_jdexpress_getorder with(nolock)";
		String sql = null;
		switch (expressId) {
		case "JD":
			sql = "select * FROM jd_need_orderinfo";
			break;
		default:
			log.error("参数不符合规范");
			return null;
		}
		List<Map<String, Object>> retList = JdbcUtils.queryPlural(sql);
		if (retList == null) {
			log.info("订单信息数据库查询返回为空");
			return null;
		}
		log.info("订单信息：" + retList.toString());
		return retList;
	}

	/**
	 * @param sono
	 * @param payment
	 * @param paytype
	 * @return
	 * @description 更新订单中的结算方式与最终货款金额
	 * @author zikc
	 * @date 2015年10月19日 下午4:49:08
	 * @update 2015年10月19日 下午4:49:08
	 * @version V1.0
	 */
	public static int updateWmsSoOrderPay(String sono, String payment,
			String paytype) {
		if (sono == null || "".equals(sono) || payment == null
				|| "".equals(payment) || paytype == null || "".equals(paytype)) {
			log.error("参数不能为空！");
			return 0;
		}
		// h_edi_01 货款金额
		// h_edi_05 结算方式
		String sql = "update doc_order_header set h_edi_01=?,h_edi_05=? where orderno=?";
		Object[] obj = { payment, paytype, sono };
		int retCount = JdbcUtils.execute(sql, obj);
		return retCount;
	}

	/**
	 * @param expressId
	 * @param deliveryNum
	 * @return
	 * @description 更新电子面单接口推送标记与时间
	 * @author zikc
	 * @date 2015年10月19日 下午4:48:37
	 * @update 2015年10月19日 下午4:48:37
	 * @version V1.0
	 */
	public static int updateDeliveryPushtime(String expressId,
			String deliveryNum) {
		if (expressId == null || "".equals(expressId) || deliveryNum == null
				|| "".equals(deliveryNum)) {
			log.error("参数不能为空！");
			return 0;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String curTime = sdf.format(System.currentTimeMillis());
		String sql = "update SYS_Delivery_No set pushflag=?,pushtime=? where customerid=? and deliveryno=? ";
		Object[] obj = { "Y", curTime, expressId, deliveryNum };
		int retCount = JdbcUtils.execute(sql, obj);
		return retCount;
	}

	/**
	 * @param orderno
	 * @param mark
	 * @return
	 * @description 更新订单中的大头笔信息
	 * @author zikc
	 * @date 2015年10月19日 下午4:47:32
	 * @update 2015年10月19日 下午4:47:32
	 * @version V1.0
	 */
	public static int updateOrderMark(String orderno, String mark) {
		if (orderno == null || "".equals(orderno) || mark == null
				|| "".equals(mark)) {
			log.error("参数不能为空！");
			return 0;
		}
		String sql = "update doc_order_header set H_EDI_04=? where orderno=?";
		Object[] obj = { mark, orderno };
		int retCount = JdbcUtils.execute(sql, obj);
		return retCount;
	}

	/**
	 * @return
	 * @description 得到中通的收货人信息
	 * @author zikc
	 * @date 2015年10月19日 下午4:47:53
	 * @update 2015年10月19日 下午4:47:53
	 * @version V1.0
	 */
	public static List<Map<String, Object>> getReceiveInfo() {
		String sql = "select * from zto_need_getmark";
		List<Map<String, Object>> retList = JdbcUtils.queryPlural(sql);
		if (retList == null) {
			log.info("中通收货信息数据库查询返回为空");
			return null;
		}
		log.info("中通收货信息为：" + retList.toString());
		return retList;
	}
}
