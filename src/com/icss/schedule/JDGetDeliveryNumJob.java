package com.icss.schedule;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.icss.service.WmsServ;
import com.jd.open.api.sdk.DefaultJdClient;
import com.jd.open.api.sdk.JdClient;
import com.jd.open.api.sdk.JdException;
import com.jd.open.api.sdk.domain.etms.OrderInfoJosService.GetResultInfoDTO;
import com.jd.open.api.sdk.request.etms.EtmsWaybillcodeGetRequest;
import com.jd.open.api.sdk.response.etms.EtmsWaybillcodeGetResponse;
import com.icss.common.CommonUtil;

public class JDGetDeliveryNumJob implements Job {
	private static Logger log = LoggerFactory
			.getLogger(JDGetDeliveryNumJob.class);

	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		try {
			log.info("------开始获取京东物流预分配的运单号------" + CommonUtil.curDate());
			getDeliveryNum();
			log.info("------获取全部完成------" + CommonUtil.curDate());
		} catch (JdException e) {
			e.printStackTrace();
		}
	}

	private void getDeliveryNum() throws JdException {
		Map<String, Object> authMap = WmsServ.getJDAuthInfo();
		if (authMap == null) {
			log.error("------没有找到京东的APP的证书信息【getJDAuthInfo】------"
					+ CommonUtil.curDate());
			return;
		}
		String SERVER_URL = "http://gw.api.jd.com/routerjson";
		String expressId = "JD";
		String appKey = String.valueOf(authMap.get("appKey"));
		String appSecret = String.valueOf(authMap.get("appSecret"));
		JdClient client = null;
		EtmsWaybillcodeGetRequest request = null;
		EtmsWaybillcodeGetResponse response = null;
		GetResultInfoDTO ret = null;
		List<Map<String, Object>> shopList = WmsServ.getJDShop();
		if (shopList == null || shopList.size() == 0) {
			log.error("------没有找到需要获取京东快递运单号的商家【getJDShop】------"
					+ CommonUtil.curDate());
			return;
		}
		Iterator<Map<String, Object>> iter = shopList.iterator();
		while (iter.hasNext()) {
			Map<String, Object> shopMap = iter.next();
			String preNum = String.valueOf(shopMap.get("preNum"));
			String customerCode = String.valueOf(shopMap.get("shopId"));
			String accessToken = String.valueOf(shopMap.get("accessToken"));
			int upLimit = Integer
					.valueOf(String.valueOf(shopMap.get("upLimit")));
			//得到京东店铺当前的可用面单号的数量
			int availableDeliveryNumCount = WmsServ
					.getJDAvailableDeliveryNumCount(customerCode);
			if(availableDeliveryNumCount>=upLimit) {
				log.info("------京东店铺当前的可用面单号数量已达到上限【当前："+availableDeliveryNumCount+" | 上限："+upLimit+"】，无需获取面单号，商家ID为【"
						+ customerCode + "】------" + CommonUtil.curDate());
				continue;
			}
			log.info("------京东店铺当前的可用面单号数量未达到上限【当前："+availableDeliveryNumCount+" | 上限："+upLimit+"】，需要获取面单号，商家ID为【"
					+ customerCode + "】------" + CommonUtil.curDate());
			request = new EtmsWaybillcodeGetRequest();
			request.setPreNum(preNum);
			request.setCustomerCode(customerCode);
			client = new DefaultJdClient(SERVER_URL, accessToken, appKey,
					appSecret);
			response = client.execute(request);
			if (Integer.parseInt(response.getCode()) > 0) {
				log.error("---京东API平台调用错误，错误码为【"
						+ response.getCode()
						+ "】【"
						+ Thread.currentThread().getStackTrace()[1]
								.getMethodName() + "】---"
						+ CommonUtil.curDate());
				continue;
			}
			ret = response.getResultInfo();
			List<String> listDeliveryNum = null;
			int retCount = 0;
			if (!"100".equals(ret.getCode())) {
				log.info("------获取京东快递运单号失败【" + ret.getMessage() + "】，商家ID为【"
						+ customerCode + "】------" + CommonUtil.curDate());
				continue;
			}
			listDeliveryNum = ret.getDeliveryIdList();
			log.info("------获取京东快递运单号成功，商家ID为【" + customerCode + "】------"
					+ CommonUtil.curDate());
			log.info("---" + listDeliveryNum.toString() + "---");
			retCount = WmsServ.updateJDShopGettime(customerCode);
			if (retCount != 1) {
				log.error("------数据库运行错误【modifyJDShopGettime】------"
						+ CommonUtil.curDate());
			} else {
				log.info("------数据库运行成功【modifyJDShopGettime】------"
						+ CommonUtil.curDate());
			}
			retCount = WmsServ.addDeliveryNum(expressId, customerCode,
					listDeliveryNum);
			if (retCount == 0) {
				log.error("------数据库运行错误【addDeliveryNum】------"
						+ CommonUtil.curDate());
			} else {
				log.info("------数据库运行成功【addDeliveryNum】------"
						+ CommonUtil.curDate());
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