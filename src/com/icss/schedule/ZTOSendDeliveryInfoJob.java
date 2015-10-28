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
	private static final String QCONF_PATH="/../config/qconf.properties";//路径为 WEB-INF/config/qconf.properties
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

	@SuppressWarnings({ "unchecked" })
	private void sendDeliveryInfo() throws JsonGenerationException,
			JsonMappingException, IOException, Exception {
		Map<String, Object> partnerMap = WmsServ.getZTOPartnerInfo();
		if (partnerMap == null || partnerMap.size()==0) {
			log.error("------没有找到中通的合作方信息【getZTOPartnerInfo】------"
					+ CommonUtil.curDate());
			return;
		}
		String serverUrl = CommonUtil.getConfProperty(SERVER_URL,QCONF_PATH);
		List<Map<String, Object>> deliveryInfo = WmsServ
				.getSendDeliveryInfo(EXPRESS_ID);
		if (deliveryInfo == null || deliveryInfo.size() == 0) {
			log.error("------没有找到中通快递需要提交的运单信息【getSendDeliveryInfo】------"
					+ CommonUtil.curDate());
			return;
		}
		Iterator<Map<String, Object>> iter = deliveryInfo.iterator();
		Map<String, Object> deliveryInfoMap=null;
		Set<String> set=null;
		Iterator<String> it=null;
		String content=null;
		String contentJson=null;
		String date=null;
		String partner=null;
		String pass=null;
		String verify=null;
		HttpPost httpPost=null;
		List<NameValuePair> params=null;
		HttpResponse httpResponse = null;
		Map<String, Object> retMap=null;
		String mailno =null;
		String sono=null;
		int retCount=0;
		String key=null;
		String newKey=null;
		Map<String, Object> senderMap = new HashMap<String, Object>();
		Map<String, Object> receiverMap = new HashMap<String, Object>();
		while (iter.hasNext()) { // 遍历List里面的每个Map
			deliveryInfoMap = iter.next();
			set = deliveryInfoMap.keySet();
			it = set.iterator();
			// 重构Map，将sender与receive独立成Map
			while (it.hasNext()) { // 遍历Map里面的Key
				key = it.next();
				if (key.startsWith("sender")) {
					newKey = key.split("_")[1]; // 取出 "_" 后面的字段作为新的key
					senderMap.clear();
					senderMap.put(newKey,
							String.valueOf(deliveryInfoMap.get(key)));
					it.remove();
				}
				if (key.startsWith("receiver")) {
					newKey = key.split("_")[1]; // 取出 "_" 后面的字段作为新的key
					senderMap.clear();
					receiverMap.put(newKey,
							String.valueOf(deliveryInfoMap.get(key)));
					it.remove();
				}
			}
			deliveryInfoMap.put("sender", senderMap);
			deliveryInfoMap.put("receiver", receiverMap);
			contentJson=CommonUtil.map2Json(deliveryInfoMap);
			content = DigestUtil.encryptBASE64(contentJson);
			date = CommonUtil.curDate();
			partner = String.valueOf(partnerMap.get("partner"));
			pass = String.valueOf(partnerMap.get("pass"));
			verify = DigestUtil.digest(partner, date, content, pass);
			httpPost = new HttpPost(serverUrl);
			params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("style", "json"));
			params.add(new BasicNameValuePair("func", "order.submit"));
			params.add(new BasicNameValuePair("partner", partner));
			params.add(new BasicNameValuePair("datetime", date));
			params.add(new BasicNameValuePair("content", content));
			params.add(new BasicNameValuePair("verify", verify));
			httpPost.setEntity(new UrlEncodedFormEntity(params, Consts.UTF_8));
			try {
				httpResponse = new DefaultHttpClient().execute(httpPost);
				retMap = CommonUtil.json2Map(EntityUtils
						.toString(httpResponse.getEntity()));
				mailno = String.valueOf(deliveryInfoMap.get("mailno"));
				sono = String.valueOf(deliveryInfoMap.get("id"));
				log.info("------中通快递运单信息为【"+contentJson+"】，运单号为【" + mailno + "】，SO订单号为【"
						+ sono + "】------" + CommonUtil.curDate());
				if ("false".equals(String.valueOf(retMap.get("result")))) {
					log.info("------提交中通快递运单信息失败【"
							+ String.valueOf(retMap.get("remark")) + "】，运单号为【"
							+ mailno + "】，SO订单号为【" + sono + "】------"
							+ CommonUtil.curDate());
					continue;
				}
				log.info("------提交中通快递运单信息成功，运单号为【" + mailno + "】，SO订单号为【"
						+ sono + "】------" + CommonUtil.curDate());
				retCount = WmsServ.updateDeliveryPushtime(EXPRESS_ID,
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
