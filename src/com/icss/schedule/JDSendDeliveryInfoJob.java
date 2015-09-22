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
import com.jd.open.api.sdk.domain.etms.OrderInfoJosService.SendResultInfoDTO;
import com.jd.open.api.sdk.domain.order.OrderDetailInfo;
import com.jd.open.api.sdk.request.etms.EtmsWaybillSendRequest;
import com.jd.open.api.sdk.request.order.OrderGetRequest;
import com.jd.open.api.sdk.response.etms.EtmsWaybillSendResponse;
import com.jd.open.api.sdk.response.order.OrderGetResponse;

public class JDSendDeliveryInfoJob implements Job {
	private final static String EXPRESS_ID="JD";
	private static Logger log = LoggerFactory
			.getLogger(JDSendDeliveryInfoJob.class);
	
	/* (non-Javadoc)
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		try {
			log.info("------开始提交京东运单信息------" + CommonUtil.curDate());
			sendDeliveryInfo();
			log.info("------提交全部完成------" + CommonUtil.curDate());
		} catch (JdException e) {
			e.printStackTrace();
		}
	}

	private void sendDeliveryInfo() throws JdException {
		Map<String, Object> authMap = WmsServ.getJDAuthInfo();
		if (authMap == null) {
			log.error("------没有找到京东的授权信息【getJDAuthInfo】------" + CommonUtil.curDate());
			return;
		}
		String SERVER_URL = "http://gw.api.jd.com/routerjson";
		String accessToken = String.valueOf(authMap.get("accessToken"));
		String appKey = String.valueOf(authMap.get("appKey"));
		String appSecret = String.valueOf(authMap.get("appSecret"));
		JdClient client = new DefaultJdClient(SERVER_URL, accessToken, appKey,
				appSecret);
		List<Map<String, Object>> deliveryInfo = WmsServ.getSendDeliveryInfo(EXPRESS_ID);
		if (deliveryInfo == null || deliveryInfo.size() == 0) {
			log.error("------没有找到京东快递需要提交的运单信息【getSendDeliveryInfo】------"
					+ CommonUtil.curDate());
			return;
		}
		Iterator<Map<String, Object>> iter = deliveryInfo.iterator();
		while (iter.hasNext()) {
			Map<String, Object> deliveryInfoMap = iter.next();
			String deliveryId = String.valueOf(deliveryInfoMap
					.get("deliveryId"));
			String salePlat = String.valueOf(deliveryInfoMap.get("salePlat"));
			String customerCode = String.valueOf(deliveryInfoMap
					.get("customerCode"));
			String orderId = String.valueOf(deliveryInfoMap.get("orderId"));
			String thrOrderId = String.valueOf(deliveryInfoMap
					.get("thrOrderId"));
			String selfPrintWayBill = String.valueOf(deliveryInfoMap
					.get("selfPrintWayBill"));
			String pickMethod = String.valueOf(deliveryInfoMap
					.get("pickMethod"));
			String packageRequired = String.valueOf(deliveryInfoMap
					.get("packageRequired"));
			String senderName = String.valueOf(deliveryInfoMap
					.get("senderName"));
			String senderAddress = String.valueOf(deliveryInfoMap
					.get("senderAddress"));
			String senderTel = String.valueOf(deliveryInfoMap.get("senderTel"));
			String senderMobile = String.valueOf(deliveryInfoMap
					.get("senderMobile"));
			String senderPostcode = String.valueOf(deliveryInfoMap
					.get("senderPostcode"));
			String receiveName = String.valueOf(deliveryInfoMap
					.get("receiveName"));
			String receiveAddress = String.valueOf(deliveryInfoMap
					.get("receiveAddress"));
			String province = String.valueOf(deliveryInfoMap.get("province"));
			String city = String.valueOf(deliveryInfoMap.get("city"));
			String county = String.valueOf(deliveryInfoMap.get("county"));
			String town = String.valueOf(deliveryInfoMap.get("town"));
//			String provinceId = String.valueOf(deliveryInfoMap.get("provinceId"));
//			String cityId = String.valueOf(deliveryInfoMap.get("cityId"));
//			String countyId = String.valueOf(deliveryInfoMap.get("countyId"));
//			String townId = String.valueOf(deliveryInfoMap.get("townId"));
//			String siteType = String.valueOf(deliveryInfoMap.get("siteType"));
//			String siteId = String.valueOf(deliveryInfoMap.get("siteId"));
			String siteName = String.valueOf(deliveryInfoMap.get("siteName"));
			String receiveTel = String.valueOf(deliveryInfoMap
					.get("receiveTel"));
			String receiveMobile = String.valueOf(deliveryInfoMap
					.get("receiveMobile"));
			String postcode = String.valueOf(deliveryInfoMap.get("postcode"));
			String packageCount = String.valueOf(deliveryInfoMap
					.get("packageCount"));
			String weight = String.valueOf(deliveryInfoMap.get("weight"));
			String vloumLong = String.valueOf(deliveryInfoMap.get("vloumLong"));
			String vloumWidth = String.valueOf(deliveryInfoMap
					.get("vloumWidth"));
			String vloumHeight = String.valueOf(deliveryInfoMap
					.get("vloumHeight"));
			String vloumn = String.valueOf(deliveryInfoMap.get("vloumn"));
			String description = String.valueOf(deliveryInfoMap
					.get("description"));
			String collectionValue = String.valueOf(deliveryInfoMap
					.get("collectionValue"));
			String collectionMoney = String.valueOf(deliveryInfoMap
					.get("collectionMoney"));
			String guaranteeValue = String.valueOf(deliveryInfoMap
					.get("guaranteeValue"));
			String guaranteeValueAmount = String.valueOf(deliveryInfoMap
					.get("guaranteeValueAmount"));
			String signReturn = String.valueOf(deliveryInfoMap
					.get("signReturn"));
			String aging = String.valueOf(deliveryInfoMap.get("aging"));
			String transType = String.valueOf(deliveryInfoMap.get("transType"));
			String remark = String.valueOf(deliveryInfoMap.get("remark"));
			String goodsType = String.valueOf(deliveryInfoMap.get("goodsType"));
			String orderType = String.valueOf(deliveryInfoMap.get("orderType"));
			String shopCode = String.valueOf(deliveryInfoMap.get("shopCode"));
			String orderSendTime = String.valueOf(deliveryInfoMap
					.get("orderSendTime"));
			String warehouseCode = String.valueOf(deliveryInfoMap
					.get("warehouseCode"));
			String extendField1 = String.valueOf(deliveryInfoMap
					.get("extendField1"));
			String extendField2 = String.valueOf(deliveryInfoMap
					.get("extendField2"));
			String extendField3 = String.valueOf(deliveryInfoMap
					.get("extendField3"));
			String extendField4 = String.valueOf(deliveryInfoMap
					.get("extendField4"));
			String extendField5 = String.valueOf(deliveryInfoMap
					.get("extendField5"));
			EtmsWaybillSendRequest request = new EtmsWaybillSendRequest();
			request.setDeliveryId(deliveryId);
			request.setSalePlat(salePlat);
			request.setCustomerCode(customerCode);
			request.setOrderId(orderId);
			request.setThrOrderId(thrOrderId);
			request.setSelfPrintWayBill(Integer.parseInt(selfPrintWayBill));
			request.setPickMethod(pickMethod);
			request.setPackageRequired(packageRequired);
			request.setSenderName(senderName);
			request.setSenderAddress(senderAddress);
			request.setSenderTel(senderTel);
			request.setSenderMobile(senderMobile);
			request.setSenderPostcode(senderPostcode);
			request.setReceiveName(receiveName);
			request.setReceiveAddress(receiveAddress);
			request.setProvince(province);
			request.setCity(city);
			request.setCounty(county);
			request.setTown(town);
//			request.setProvinceId(Integer.parseInt(provinceId));
//			request.setCityId(Integer.parseInt(cityId));
//			request.setCountyId(Integer.parseInt(countyId));
//			request.setTownId(Integer.parseInt(townId));
//			request.setSiteType(Integer.parseInt(siteType));
//			request.setSiteId(Integer.parseInt(siteId));
			request.setSiteName(siteName);
			request.setReceiveTel(receiveTel);
			request.setReceiveMobile(receiveMobile);
			request.setPostcode(postcode);
			request.setPackageCount(Integer.parseInt(packageCount));
			request.setWeight(Double.parseDouble(weight));
			request.setVloumLong(Double.parseDouble(vloumLong));
			request.setVloumWidth(Double.parseDouble(vloumWidth));
			request.setVloumHeight(Double.parseDouble(vloumHeight));
			request.setVloumn(Double.parseDouble(vloumn));
			request.setDescription(description);
			OrderGetRequest orderRequest=new OrderGetRequest();
			orderRequest.setOrderId(thrOrderId);
			orderRequest.setOptionalFields("pay_type,order_payment");
			OrderGetResponse orderResponse=client.execute(orderRequest);
			if(Integer.parseInt(orderResponse.getCode())>0) {
				log.error("---京东API平台调用错误，错误码为【"+orderResponse.getCode()+"】【"
						+Thread.currentThread().getStackTrace()[1].getMethodName()+"】---"
						+ CommonUtil.curDate());
				continue;
			}
			OrderDetailInfo odi=orderResponse.getOrderDetailInfo();
			Double collectionMoneyd=0.00d;
			int collectionValuei=1;
			if (odi.getOrderInfo() == null) {
				log.error("---获取京东订单信息【代收货款】信息失败，SO订单号为【" + orderId + "】，京东订单号为【" + thrOrderId + "】，运单号为【" + deliveryId + "】---" + CommonUtil.curDate());
				collectionMoneyd=Double.parseDouble(collectionMoney);
				log.info("---置【货款金额】为：【"+collectionMoney+"】，SO订单号为【" + orderId + "】，京东订单号为【" + thrOrderId + "】，运单号为【" + deliveryId + "】---"+ CommonUtil.curDate());
			} else {
				String orderPayment=odi.getOrderInfo().getOrderPayment();
				String orderPaytype=odi.getOrderInfo().getPayType().split("-")[0];
				collectionMoneyd=Double.parseDouble(orderPayment);
				collectionValuei=Integer.parseInt(orderPaytype);
				log.info("---获取京东订单代收货款信息成功，金额为：【"+collectionMoneyd+"】，订单类型：【"+orderPaytype+"】，SO订单号为【" + orderId + "】，京东订单号为【" + thrOrderId + "】，运单号为【" + deliveryId + "】---" + CommonUtil.curDate());
			}
			request.setCollectionValue(collectionValuei);
			request.setCollectionMoney(collectionMoneyd);
			request.setGuaranteeValue(Integer.parseInt(guaranteeValue));
			request.setGuaranteeValueAmount(Double
					.parseDouble(guaranteeValueAmount));
			request.setSignReturn(Integer.parseInt(signReturn));
			request.setAging(Integer.parseInt(aging));
			request.setTransType(Integer.parseInt(transType));
			request.setRemark(remark);
			request.setGoodsType(Integer.parseInt(goodsType));
			request.setOrderType(Integer.parseInt(orderType));
			request.setShopCode(shopCode);
			request.setOrderSendTime(orderSendTime);
			request.setWarehouseCode(warehouseCode);
			request.setExtendField1(extendField1);
			request.setExtendField2(extendField2);
			request.setExtendField3(extendField3);
			request.setExtendField4(Integer.parseInt(extendField4));
			request.setExtendField5(Integer.parseInt(extendField5));
			EtmsWaybillSendResponse response = client.execute(request);
			if(Integer.parseInt(response.getCode())>0) {
				log.error("---京东API平台调用错误，错误码为【"+response.getCode()+"】【"
						+Thread.currentThread().getStackTrace()[1].getMethodName()+"】---"
						+ CommonUtil.curDate());
				continue;
			}
			SendResultInfoDTO ret = response.getResultInfo();
			if (!"100".equals(ret.getCode())) {
				log.info("---提交京东快递运单信息失败【" + ret.getMessage() + "】，运单号为【"
						+ deliveryId + "】，京东订单号为【" + thrOrderId + "】，SO订单号为【" + orderId + "】---"
						+ CommonUtil.curDate());
				continue;
			}
			log.info("---提交京东快递运单信息成功，运单号为【" + deliveryId + "】，京东订单号为【" + thrOrderId + "】，SO订单号为【"
					+ orderId + "】---" + CommonUtil.curDate());
			int retCount=WmsServ.updateDeliveryPushtime(EXPRESS_ID,ret.getDeliveryId());
			if (retCount != 1) {
				log.error("---数据库运行错误【modifyDeliveryPushtime】---"
						+ CommonUtil.curDate());
			} else {
				log.info("---数据库运行成功【modifyDeliveryPushtime】---"
						+ CommonUtil.curDate());
			}
		}
	}
}
