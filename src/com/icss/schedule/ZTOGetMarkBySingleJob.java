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
	private static final String QCONF_PATH="/../config/qconf.properties";//路径为 WEB-INF/config/qconf.properties
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
		String sendCity = String.valueOf(partnerMap.get("sendCity"));
		String sendAddress = String.valueOf(partnerMap.get("sendAddress"));
		String serverUrl = CommonUtil.getConfProperty(SERVER_URL,QCONF_PATH);
		List<Map<String, Object>> receiveInfo = WmsServ.getReceiveInfo();
		if (receiveInfo == null || receiveInfo.size() == 0) {
			log.error("---没有找到需要的中通收货信息【"
					+ Thread.currentThread().getStackTrace()[1].getMethodName()
					+ "】---" + CommonUtil.curDate());
			return;
		}
		Iterator<Map<String, Object>> iter = receiveInfo.iterator();
		HttpPost httpPost = null;
		List<NameValuePair> params = null;
		HttpResponse httpResponse = null;
		while (iter.hasNext()) {
			Map<String, Object> receiveInfoMap = iter.next();
			Map<String, Object> receiveInfoSendMap = new HashMap<String, Object>();
			receiveInfoSendMap.put("sendcity", sendCity);
			receiveInfoSendMap.put("sendaddress", sendAddress);
			String receivecityStr = String.valueOf(receiveInfoMap
					.get("receivercity"));
			String receiveraddressStr = String.valueOf(receiveInfoMap
					.get("receiveraddress"));
			// //增加判断最后一位【区县】是否为空字符串
			// if(receivecityStr.endsWith(","))
			// receivecityStr=receivecityStr.substring(0,
			// receivecityStr.length()-1);
			receiveInfoSendMap.put("receivercity", receivecityStr);
			receiveInfoSendMap.put("receiveraddress", receiveraddressStr);
			String content = DigestUtil.encryptBASE64(CommonUtil
					.map2Json(receiveInfoSendMap));
			String date = CommonUtil.curDate();
			String partner = String.valueOf(partnerMap.get("partner"));
			String pass = String.valueOf(partnerMap.get("pass"));
			String verify = DigestUtil.digest(partner, date, content, pass);
			httpPost = new HttpPost(serverUrl);
			params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("style", "json"));
			params.add(new BasicNameValuePair("func", "order.marke"));
			params.add(new BasicNameValuePair("partner", partner));
			params.add(new BasicNameValuePair("datetime", date));
			params.add(new BasicNameValuePair("content", content));
			params.add(new BasicNameValuePair("verify", verify));
			httpPost.setEntity(new UrlEncodedFormEntity(params, Consts.UTF_8));
			try {
				httpResponse = new DefaultHttpClient().execute(httpPost);
				String retStr = EntityUtils.toString(httpResponse.getEntity()); // 返回的大头笔
				String sono = String.valueOf(receiveInfoMap.get("orderno")); // SO订单号
				boolean isRetSuccess = true;
				Map<String, Object> retMap =null;
				String mark=null; //最终要更新的大头笔
				if (retStr == null || "".equals(retStr)) {
					log.info("------获取中通大头笔失败，返回报文为null或空字符串，将以城市作为大头笔，收件人省市区为【"
							+ receivecityStr + "】，SO订单号为【" + sono + "】------"
							+ CommonUtil.curDate());
					isRetSuccess = false;
				} else {
					retMap = CommonUtil.json2Map(retStr);
					if (retMap != null
							&& "false".equals(String.valueOf(retMap
									.get("result")))) {
						log.info("------获取中通大头笔失败【"
								+ String.valueOf(retMap.get("remark"))
								+ "】，将以城市作为大头笔，收件人省市区为【" + receivecityStr
								+ "】，SO订单号为【" + sono + "】------"
								+ CommonUtil.curDate());
						isRetSuccess = false;
					}
				}
				if (isRetSuccess&&retMap.get("mark")!=null) {
					mark=String.valueOf(retMap.get("mark"));
					if (!"".equals(mark)) {
					log.info("------获取中通大头笔成功【" +mark+ "】，收件人省市区为【"
							+ receivecityStr + "】，SO订单号为【" + sono + "】------"
							+ CommonUtil.curDate());
					} else {
						mark= receivecityStr.split(",")[1];// 将城市作为最终大头笔
						log.info("------获取中通大头笔失败，返回的大头笔字段为【空字符串】，将以城市作为大头笔，收件人省市区为【"
								+ receivecityStr + "】，SO订单号为【" + sono + "】------"
								+ CommonUtil.curDate());
					}
				} else {
					mark= receivecityStr.split(",")[1];// 将城市作为最终大头笔
					log.info("------获取中通大头笔失败，返回的大头笔字段为【空对象】，将以城市作为大头笔，收件人省市区为【"
							+ receivecityStr + "】，SO订单号为【" + sono + "】------"
							+ CommonUtil.curDate());
				}
				int retCount = WmsServ.updateOrderMark(sono, mark);
				if (retCount != 1) {
					log.error("------数据库运行错误【updateOrderMark】------"
							+ CommonUtil.curDate());
				} else {
					log.info("------数据库运行成功【updateOrderMark】------"
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
