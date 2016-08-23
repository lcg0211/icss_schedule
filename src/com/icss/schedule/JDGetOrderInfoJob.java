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
import com.icss.service.WmsServ;
import com.jd.open.api.sdk.DefaultJdClient;
import com.jd.open.api.sdk.JdClient;
import com.jd.open.api.sdk.JdException;
import com.jd.open.api.sdk.domain.order.OrderDetailInfo;
import com.jd.open.api.sdk.request.order.OrderGetRequest;
import com.jd.open.api.sdk.response.order.OrderGetResponse;

public class JDGetOrderInfoJob implements Job {
	private final static String EXPRESS_ID = "JD";
	private static Logger log = LoggerFactory
			.getLogger(JDGetOrderInfoJob.class);

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		// TODO Auto-generated method stub
		try {
			log.info("------开始获取京东订单信息------" + CommonUtil.curDate());
			getOrderInfo();
			log.info("------提交全部完成------" + CommonUtil.curDate());
		} catch (JdException e) {
			e.printStackTrace();
		}

	}

	@SuppressWarnings({ "deprecation", "unchecked", "resource", "rawtypes" })
	private void getOrderInfo() throws JdException {
		Map<String, Object> authMap = WmsServ.getJDAuthInfo();
		if (authMap == null) {
			log.error("------没有找到京东的APP的证书信息【getJDAuthInfo】------"
					+ CommonUtil.curDate());
			return;
		}
		//String serverUrl = "http://localhost:8081/service/getPayment";
		String serverUrl = "http://153.36.228.172/service/getPayment";
		// String serverUrl="153.36.228.172";
		String sourceOrderNo = null;
		String soNo = null;
		String accessToken = null;
		String optionalFields = "pay_type,order_payment";
		String orderPayment = null;
		String orderPaytype = null;
		List<Map<String, Object>> orderInfo = WmsServ.getOrderInfo(EXPRESS_ID);
		if (orderInfo == null || orderInfo.size() == 0) {
			log.error("---没有找到需要的京东订单信息【getOrderInfo】---"
					+ CommonUtil.curDate());
			return;
		}
		Iterator<Map<String, Object>> iter = orderInfo.iterator();
		HttpPost httpPost = null;
		List<NameValuePair> params = null;
		while (iter.hasNext()) {
			Map<String, Object> orderInfoMap = iter.next();
			sourceOrderNo = String.valueOf(orderInfoMap
					.get("source_orderno"));
			soNo = String.valueOf(orderInfoMap
					.get("sono"));
			accessToken = String.valueOf(orderInfoMap.get("accessToken"));
			httpPost = new HttpPost(serverUrl);
			params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("so_no", soNo));
			params.add(new BasicNameValuePair("source_order_no", sourceOrderNo));
			params.add(new BasicNameValuePair("access_token", accessToken));
			params.add(new BasicNameValuePair("optional_fields", optionalFields));
			httpPost.setEntity(new UrlEncodedFormEntity(params, Consts.UTF_8));
			HttpResponse httpResponse;
			try {
				httpResponse = new DefaultHttpClient().execute(httpPost);
				Map<String, Object> retMap = CommonUtil.json2Map(EntityUtils
						.toString(httpResponse.getEntity()));
				if (retMap.get("result") == null) {
					log.error("错误，京东代理API返回对象为空【getOrderInfo】，SO订单号为【" + soNo
							+ "】，京东订单号为【" + sourceOrderNo + "】---"
							+ CommonUtil.curDate());
					return;
				}
				if ("fail".equals(String.valueOf(retMap.get("result")))) {
					log.error("错误，京东代理API返回错误【"
							+ String.valueOf(retMap.get("data")) + "】，SO订单号为【"
							+ soNo + "】，京东订单号为【" + sourceOrderNo + "】---"
							+ CommonUtil.curDate());
					return;
				}
				if ("success".equals(String.valueOf(retMap.get("result")))) {
					Map map = (Map)retMap.get("data");
					soNo = String.valueOf(map.get("so_no"));
					orderPayment = String.valueOf(map.get("order_payment"));
					orderPaytype = String.valueOf(map.get("order_paytype"));
					log.info("正确，京东代理API返回正确，获取京东订单信息成功，金额为：【" + orderPayment
							+ "】，支付方式为：【" + orderPaytype + "】，SO订单号为【" + soNo
							+ "】---" + CommonUtil.curDate());
					int retCount = WmsServ.updateWmsSoOrderPay(soNo,
							orderPayment, orderPaytype);
					if (retCount != 1) {
						log.error("---数据库运行错误【updateWmsSoOrderPay】，SO订单号为【"
								+ soNo + "】---"
								+ CommonUtil.curDate());
					} else {
						log.info("---数据库运行成功【updateWmsSoOrderPay】，SO订单号为【"
								+ soNo + "】---"
								+ CommonUtil.curDate());
					}
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
