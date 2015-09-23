package com.icss.schedule;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
	private final static String EXPRESS_ID="JD";
	private static Logger log = LoggerFactory
			.getLogger(JDGetOrderInfoJob.class);
	
	/* (non-Javadoc)
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		try {
			log.info("------开始获取京东订单信息------" + CommonUtil.curDate());
			getOrderInfo();
			log.info("------提交全部完成------" + CommonUtil.curDate());
		} catch (JdException e) {
			e.printStackTrace();
		}
	}

	private void getOrderInfo() throws JdException {
		Map<String, Object> authMap = WmsServ.getJDAuthInfo();
		if (authMap == null) {
			log.error("------没有找到京东的APP的证书信息【getJDAuthInfo】------" + CommonUtil.curDate());
			return;
		}
		String SERVER_URL = "http://gw.api.jd.com/routerjson";
		String appKey = String.valueOf(authMap.get("appKey"));
		String appSecret = String.valueOf(authMap.get("appSecret"));
		JdClient client = null;
		OrderGetRequest orderRequest=null;
		OrderGetResponse orderResponse=null;
		OrderDetailInfo odi=null;
		String sourceOrderNo=null;
		String soNo=null;
		String accessToken=null;
		String orderPayment = null;
		String orderPaytype=null;
		List<Map<String, Object>> orderInfo = WmsServ.getOrderInfo(EXPRESS_ID);
		if (orderInfo == null || orderInfo.size() == 0) {
			log.error("---没有找到需要的京东订单信息【"+Thread.currentThread().getStackTrace()[1].getMethodName()+"】---"+ CommonUtil.curDate());
			return;
		}
		Iterator<Map<String, Object>> iter = orderInfo.iterator();
		while (iter.hasNext()) {
			Map<String, Object> orderInfoMap = iter.next();
			sourceOrderNo = String.valueOf(orderInfoMap
					.get("source_orderno"));
			soNo = String.valueOf(orderInfoMap
					.get("sono"));
			accessToken = String.valueOf(orderInfoMap.get("accessToken"));
			orderRequest=new OrderGetRequest();
			orderRequest.setOrderId(sourceOrderNo);
			orderRequest.setOptionalFields("pay_type,order_payment");
			client=new DefaultJdClient(SERVER_URL, accessToken, appKey,
					appSecret);
			orderResponse=client.execute(orderRequest);
			if(Integer.parseInt(orderResponse.getCode())>0) {
				log.error("---京东API平台调用错误，错误码为【"+orderResponse.getCode()+"】【"
						+Thread.currentThread().getStackTrace()[1].getMethodName()+"】---"
						+ CommonUtil.curDate());
				continue;
			}
			odi=orderResponse.getOrderDetailInfo();
			if (odi.getOrderInfo() == null) {
				log.error("---获取京东订单信息失败，SO订单号为【" + soNo + "】，京东订单号为【" + sourceOrderNo + "】---" + CommonUtil.curDate());
				continue;
			}
			orderPayment=odi.getOrderInfo().getOrderPayment();
			orderPaytype=odi.getOrderInfo().getPayType();
				log.info("---获取京东订单信息成功，金额为：【"+orderPayment+"】，支付方式为：【"+orderPaytype+"】，SO订单号为【" + soNo + "】，京东订单号为【" + sourceOrderNo + "】---" + CommonUtil.curDate());
			int retCount=WmsServ.updateWmsSoOrderPay(soNo,orderPayment,orderPaytype);
			if (retCount != 1) {
				log.error("---数据库运行错误【updateWmsSoOrderPay】，SO订单号为【" + soNo + "】---"
						+ CommonUtil.curDate());
			} else {
				log.info("---数据库运行成功【updateWmsSoOrderPay】，SO订单号为【" + soNo + "】---"
						+ CommonUtil.curDate());
			}
		}
	}
}
