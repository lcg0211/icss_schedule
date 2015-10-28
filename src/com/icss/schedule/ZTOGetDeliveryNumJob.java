package com.icss.schedule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
public class ZTOGetDeliveryNumJob implements Job {
	private final static String EXPRESS_ID = "ZTO";
	private final static String SERVER_URL = "ZTOServerUrl";
	private static final String QCONF_PATH="/../config/qconf.properties";//路径为 WEB-INF/config/qconf.properties
	private static Logger log = LoggerFactory
			.getLogger(ZTOGetDeliveryNumJob.class);

	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		log.info("------开始获取中通运单号------" + CommonUtil.curDate());
		try {
			getDeliveryNum();
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.info("------获取全部完成------" + CommonUtil.curDate());
	}

	@SuppressWarnings({ "resource", "unchecked" })
	private void getDeliveryNum() throws JsonGenerationException,
			JsonMappingException, IOException, Exception {
		Map<String, Object> partnerMap = WmsServ.getZTOPartnerInfo();
		if (partnerMap == null || partnerMap.size()==0) {
			log.error("------没有找到中通的合作方信息【getZTOPartnerInfo】------"
					+ CommonUtil.curDate());
			return;
		}
		String serverUrl = CommonUtil.getConfProperty(SERVER_URL,QCONF_PATH);
		Map<String, Object> mailApplyMap = new HashMap<String, Object>();
		int number = Integer.parseInt(String.valueOf(partnerMap.get("number")));
		int upLimit=Integer.parseInt(String.valueOf(partnerMap.get("upLimit")));
		//得到中通当前的可用面单号的数量
		int availableDeliveryNumCount = WmsServ
				.getExpressAvailableDeliveryNumCount(EXPRESS_ID);
		if(availableDeliveryNumCount>=upLimit) {
			log.info("------ 中通当前的可用面单号数量已达到上限【当前："+availableDeliveryNumCount+" | 上限："+upLimit+"】，无需获取面单号 ------" + CommonUtil.curDate());
			return;
		}
		log.info("------ 中通当前的可用面单号数量未达到上限【当前："+availableDeliveryNumCount+" | 上限："+upLimit+"】，需要获取面单号 ------" + CommonUtil.curDate());
		mailApplyMap.put("number", number);
		mailApplyMap.put("lastno", String.valueOf(partnerMap.get("lastNo")));
		String content = DigestUtil.encryptBASE64(CommonUtil
				.map2Json(mailApplyMap));
		String date = CommonUtil.curDate();
		String partner = String.valueOf(partnerMap.get("partner"));
		String pass = String.valueOf(partnerMap.get("pass"));
		String verify = DigestUtil.digest(partner, date, content, pass);
		HttpPost httpPost = new HttpPost(serverUrl);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("style", "json"));
		params.add(new BasicNameValuePair("func", "mail.apply"));
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
			if ("false".equals(String.valueOf(retMap.get("result")))) {
				log.info("------获取中通运单号失败【"
						+ String.valueOf(retMap.get("remark")) + "】------"
						+ CommonUtil.curDate());
				return;
			}
			List<Long> retMailNo = (List<Long>) retMap.get("list");
			Long lastNo = retMailNo.get(number-1);
			log.info("------获取中通运单号成功，运单号为【" + retMailNo.toString()
					+ "】，最后一个运单号码为【" + lastNo.toString() + "】------"
					+ CommonUtil.curDate());
			int retCount = WmsServ.updateZTOGettime(partner, lastNo.toString());
			if (retCount != 1) {
				log.error("------数据库运行错误【updateZTOGettime】------"
						+ CommonUtil.curDate());
				return;
			} else {
				log.info("------数据库运行成功【updateZTOGettime】------"
						+ CommonUtil.curDate());
			}
			retCount = WmsServ.addDeliveryNumZTO(EXPRESS_ID, partner, retMailNo);
			if (retCount == 0) {
				log.error("------数据库运行错误【addDeliveryNum】------"
						+ CommonUtil.curDate());
				return;
			} else {
				log.info("------数据库运行成功【addDeliveryNum】------"
						+ CommonUtil.curDate());
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
