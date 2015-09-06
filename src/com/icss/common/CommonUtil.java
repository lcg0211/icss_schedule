package com.icss.common;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class CommonUtil {
	public static String CurDate() {
	SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	return sdf.format(System.currentTimeMillis());
}
	public static String FormattedDate(Date dt) {
		SimpleDateFormat sdf = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		return sdf.format(dt);
	}
}
