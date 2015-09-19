package com.icss.schedule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.icss.common.CommonUtil;
import com.icss.common.DigestUtil;
import com.icss.service.WmsServ;

@SuppressWarnings("deprecation")
public class ZTOGetMarkBySingleJob implements Job {
	private final static String SERVER_URL = "ZTOServerUrl";
	private static Logger log = LoggerFactory
			.getLogger(ZTOSendDeliveryInfoJob.class);

	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		log.info("------开始获取中通大头笔------" + CommonUtil.curDate());
		try {
			GetMarkBySingle();
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.info("------获取全部完成------" + CommonUtil.curDate());
	}

	@SuppressWarnings({ "unchecked", "resource" })
	private void GetMarkBySingle() throws Exception {
		Map<String, Object> partnerMap = WmsServ.getZTOPartnerInfo();
		if (partnerMap == null) {
			log.error("------没有找到中通的合作方信息【getZTOPartnerInfo】------"
					+ CommonUtil.curDate());
			return;
		}
		String serverUrl = CommonUtil.getConfProperty(SERVER_URL);
		List<Map<String, Object>> receiveInfo = WmsServ.getReceiveInfo();
		Iterator<Map<String, Object>> iter = receiveInfo.iterator();
		while (iter.hasNext()) {
			Map<String, Object> receiveInfoMap = iter.next();
			Map<String, Object> receiveInfoSendMap = new HashMap<String, Object>();
			receiveInfoSendMap.put("sendcity", "浙江省,宁波市,慈溪市");
			String receivecityStr = String.valueOf(receiveInfoMap
					.get("receivercity"));
			receiveInfoSendMap.put("receivercity", receivecityStr);
			String content = DigestUtil.encryptBASE64(CommonUtil
					.map2Json(receiveInfoSendMap));
			String date = CommonUtil.curDate();
			String partner = String.valueOf(partnerMap.get("partner"));
			String pass = String.valueOf(partnerMap.get("pass"));
			String verify = DigestUtil.digest(partner, date, content, pass);
			HttpPost httpPost = new HttpPost(serverUrl);
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("style", "json"));
			params.add(new BasicNameValuePair("func", "order.marke"));
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
				// String mailno =
				// String.valueOf(deliveryInfoMap.get("mailno"));
				String sono = String.valueOf(receiveInfoMap.get("orderno")); // SO订单号
				if ("false".equals(String.valueOf(retMap.get("result")))) {
					log.info("------获取中通大头笔失败【"
							+ String.valueOf(retMap.get("remark"))
							+ "】，SO订单号为【" + sono + "】------"
							+ CommonUtil.curDate());
					return;
				}
				log.info("------获取中通大头笔成功，SO订单号为【" + sono + "】------"
						+ CommonUtil.curDate());
				String mark = String.valueOf(retMap.get("marke")); // 得到的大头笔
				int retCount = WmsServ.updateReceiveInfo(sono, receivecityStr,
						mark);
				if (retCount != 1) {
					log.error("------数据库运行错误【updateReceiveInfo】------"
							+ CommonUtil.curDate());
				} else {
					log.info("------数据库运行成功【updateReceiveInfo】------"
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
