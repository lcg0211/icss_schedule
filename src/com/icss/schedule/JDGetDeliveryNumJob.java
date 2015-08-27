package com.icss.schedule;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.icss.service.WmsBusi;
import com.jd.open.api.sdk.DefaultJdClient;
import com.jd.open.api.sdk.JdClient;
import com.jd.open.api.sdk.JdException;
import com.jd.open.api.sdk.domain.etms.OrderInfoJosService.GetResultInfoDTO;
import com.jd.open.api.sdk.request.etms.EtmsWaybillcodeGetRequest;
import com.jd.open.api.sdk.response.etms.EtmsWaybillcodeGetResponse;

public class JDGetDeliveryNumJob implements Job {
	private static Logger log = LoggerFactory
			.getLogger(JDGetDeliveryNumJob.class);

	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		try {
			log.info("------开始获取京东物流预分配的运单号------" + new Date());
			getDeliveryNum();
			log.info("------获取全部完成------" + new Date());
		} catch (JdException e) {
			e.printStackTrace();
		}
	}

	private void getDeliveryNum() throws JdException {
		Map<String, Object> authMap = WmsBusi.getJDAuthInfo();
		if (authMap == null) {
			log.error("------没有找到京东的授权信息【getJDAuthInfo】------" + new Date());
			return;
		}
		String SERVER_URL = "http://gw.api.jd.com/routerjson";
		String expressId = "JD";
		String accessToken = String.valueOf(authMap.get("accessToken"));
		String appKey = String.valueOf(authMap.get("appKey"));
		String appSecret = String.valueOf(authMap.get("appSecret"));
		JdClient client = new DefaultJdClient(SERVER_URL, accessToken, appKey,
				appSecret);
		List<Map<String, Object>> shopList = WmsBusi.getJDShop();
		if (shopList == null || shopList.size() == 0) {
			log.error("------没有找到需要获取京东快递运单号的商家【getJDShop】------" + new Date());
			return;
		}
		Iterator<Map<String, Object>> iter = shopList.iterator();
		while (iter.hasNext()) {
			Map<String, Object> shopMap = iter.next();
			String preNum = String.valueOf(shopMap.get("preNum"));
			String customerCode = String.valueOf(shopMap.get("shopId"));
			EtmsWaybillcodeGetRequest request = new EtmsWaybillcodeGetRequest();
			request.setPreNum(preNum);
			request.setCustomerCode(customerCode);
			EtmsWaybillcodeGetResponse response = client.execute(request);
			if(Integer.parseInt(response.getCode())>0) {
				log.error("---京东API平台调用错误，错误码为【"+response.getCode()+"】【"
						+Thread.currentThread().getStackTrace()[1].getMethodName()+"】---"
						+ new Date());
				return;
			}
			GetResultInfoDTO ret = response.getResultInfo();
			List<String> listDeliveryNum = null;
			int retCount = 0;
			if (!"100".equals(ret.getCode())) {
				log.info("------获取京东快递运单号失败【"+ret.getMessage()+"】，商家ID为【" + customerCode + "】------"
						+ new Date());
				return;
			}
			listDeliveryNum = ret.getDeliveryIdList();
			log.info("------获取京东快递运单号成功，商家ID为【" + customerCode + "】------"
					+ new Date());
			log.info("---" + listDeliveryNum.toString() + "---");
			retCount = WmsBusi.modifyJDShopGettime(customerCode);
			if (retCount != 1) {
				log.error("------数据库运行错误【modifyJDShopGettime】------"
						+ new Date());
			} else {
				log.info("------数据库运行成功【modifyJDShopGettime】------"
						+ new Date());
			}
			retCount = WmsBusi.addDeliveryNum(expressId, customerCode,
					listDeliveryNum);
			if (retCount == 0) {
				log.error("------数据库运行错误【addDeliveryNum】------" + new Date());
			} else {
				log.info("------数据库运行成功【addDeliveryNum】------" + new Date());
			}
		}
	}

	// @SuppressWarnings({ "unchecked", "finally" })
	// private String readJson2Map(String json) {
	// ObjectMapper objectMapper = new ObjectMapper();
	// StringBuffer sb=new StringBuffer();
	// try {
	// System.out.println(json);
	// Map<String, Object> maps = objectMapper.readValue(json, Map.class);
	// System.out.println(maps.size());
	// Iterator<String> iter = maps.keySet().iterator();
	// while (iter.hasNext()) {
	// String field = iter.next();
	// sb.append(field + ":" + maps.get(field));
	// System.out.println(field + ":" + maps.get(field));
	// }
	// } catch (IOException e) {
	// e.printStackTrace();
	// } finally {
	// return sb.toString();
	// }
	// }
}