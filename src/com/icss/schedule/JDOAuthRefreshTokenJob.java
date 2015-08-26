package com.icss.schedule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
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

import com.icss.service.WmsBusi;

@SuppressWarnings("deprecation")
public class JDOAuthRefreshTokenJob implements Job {
	private static Logger log = LoggerFactory
			.getLogger(JDOAuthRefreshTokenJob.class);

	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		log.info("------开始刷新令牌------" + new Date());
		oAuthRefreshToken();
		log.info("------刷新令牌完成------" + new Date());

	}

	@SuppressWarnings({ "resource" })
	private void oAuthRefreshToken() {
		WmsBusi bi = new WmsBusi();
		Map<String, Object> authMap = bi.getJDAuthInfo();
		if (authMap == null) {
			log.error("------没有找到京东的授权信息【getJDAuthInfo】------" + new Date());
			return;
		}
		try {
			// String accessToken = String.valueOf(authMap.get("accessToken"));
			String appKey = String.valueOf(authMap.get("appKey"));
			String appSecret = String.valueOf(authMap.get("appSecret"));
			String refreshToken = String.valueOf(authMap.get("refreshToken"));
			HttpPost httpPost = new HttpPost("https://oauth.jd.com/oauth/token");
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("grant_type", "refresh_token"));
			params.add(new BasicNameValuePair("client_id", appKey));
			params.add(new BasicNameValuePair("client_secret", appSecret));
			params.add(new BasicNameValuePair("refresh_token", refreshToken));
			HttpResponse httpResponse;
			httpPost.setEntity(new UrlEncodedFormEntity(params, Consts.UTF_8));
			httpResponse = new DefaultHttpClient().execute(httpPost);
			log.info(EntityUtils.toString(httpResponse.getEntity()));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
