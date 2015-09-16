package com.icss.schedule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.icss.common.CommonUtil;
import com.icss.common.DigestUtil;
import com.icss.service.WmsServ;

@SuppressWarnings("deprecation")
public class ZTOSendDeliveryInfoJob implements Job {
	private final static String EXPRESS_ID = "ZTO";
	private final static String SERVER_URL = "ZTOServerUrl";
	private static Logger log = LoggerFactory
			.getLogger(ZTOSendDeliveryInfoJob.class);

	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		log.info("------开始提交中通运单信息------" + CommonUtil.curDate());
		try {
			sendDeliveryInfo();
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.info("------提交全部完成------" + CommonUtil.curDate());
	}

	@SuppressWarnings({ "resource", "unchecked" })
	private void sendDeliveryInfo() throws JsonGenerationException,
			JsonMappingException, IOException, Exception {
		Map<String, Object> partnerMap = WmsServ.getZTOPartnerInfo();
		if (partnerMap == null) {
			log.error("------没有找到中通的合作方信息【getZTOPartnerInfo】------"
					+ CommonUtil.curDate());
			return;
		}
		String serverUrl = CommonUtil.getConfProperty(SERVER_URL);

		List<Map<String, Object>> deliveryInfo = WmsServ
				.getSendDeliveryInfo(EXPRESS_ID);
		if (deliveryInfo == null || deliveryInfo.size() == 0) {
			log.error("------没有找到中通快递需要提交的运单信息【getSendDeliveryInfo】------"
					+ CommonUtil.curDate());
			return;
		}
		Iterator<Map<String, Object>> iter = deliveryInfo.iterator();
		while (iter.hasNext()) { // 遍历Lis里面的每个Map
			Map<String, Object> deliveryInfoMap = iter.next();
			Set<String> set = deliveryInfoMap.keySet();
			Iterator<String> it = set.iterator();
			Map<String, Object> senderMap = new HashMap<String, Object>();
			Map<String, Object> receiverMap = new HashMap<String, Object>();
			// 重构Map，将sender与receive独立成Map
			while (it.hasNext()) { // 遍历Map里面的Key
				String key = it.next();
				if (key.startsWith("sender")) {
					String newKey = key.split("_")[1]; // 取出 "_" 后面的字段作为新的key
					System.out.println(newKey);
					senderMap.put(newKey,
							String.valueOf(deliveryInfoMap.get(key)));
					it.remove();
				}
				if (key.startsWith("receiver")) {
					String newKey = key.split("_")[1]; // 取出 "_" 后面的字段作为新的key
					System.out.println(newKey);
					receiverMap.put(newKey,
							String.valueOf(deliveryInfoMap.get(key)));
					it.remove();
				}
			}
			deliveryInfoMap.put("sender", senderMap);
			deliveryInfoMap.put("receiver", receiverMap);
			String content = DigestUtil.encryptBASE64(CommonUtil
					.map2Json(deliveryInfoMap));
			String date = CommonUtil.curDate();
			String partner = String.valueOf(partnerMap.get("partner"));
			String pass = String.valueOf(partnerMap.get("pass"));
			String verify = DigestUtil.digest(partner, date, content, pass);
			HttpPost httpPost = new HttpPost(serverUrl);
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("style", "json"));
			params.add(new BasicNameValuePair("func", "order.submit"));
			params.add(new BasicNameValuePair("partner", partner));
			params.add(new BasicNameValuePair("datetime", date));
			params.add(new BasicNameValuePair("content", content));
			params.add(new BasicNameValuePair("verify", verify));
			HttpResponse httpResponse = null;
			httpPost.setEntity(new UrlEncodedFormEntity(params, Consts.UTF_8));
			try {
				httpResponse = new DefaultHttpClient().execute(httpPost);
				Map<String, Object> retMap = CommonUtil.json2Map(EntityUtils
						.toString(httpResponse.getEntity()));
				String mailno = String.valueOf(deliveryInfoMap.get("mailno"));
				String sono = String.valueOf(deliveryInfoMap.get("id"));
				if ("false".equals(String.valueOf(retMap.get("result")))) {
					log.info("------提交中通快递运单信息失败【"
							+ String.valueOf(retMap.get("remark")) + "】，运单号为【"
							+ mailno + "】，SO订单号为【" + sono + "】------"
							+ CommonUtil.curDate());
					return;
				}
				log.info("------提交中通快递运单信息成功，运单号为【" + mailno + "】，SO订单号为【"
						+ sono + "】------" + CommonUtil.curDate());
				int retCount = WmsServ.updateDeliveryPushtime(EXPRESS_ID,
						mailno);
				if (retCount != 1) {
					log.error("------数据库运行错误【modifyDeliveryPushtime】------"
							+ CommonUtil.curDate());
				} else {
					log.info("------数据库运行成功【modifyDeliveryPushtime】------"
							+ CommonUtil.curDate());
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
