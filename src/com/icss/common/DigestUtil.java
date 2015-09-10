package com.icss.common;

import java.security.MessageDigest;

import sun.misc.BASE64Encoder;

/**
 * @description 生成摘要工具类
 * @author zikc
 * @date 2015年9月10日 下午2:23:37 
 * @update 2015年9月10日 下午2:23:37 
 * @version V1.0
 */
public class DigestUtil {
    public static final String UTF8="UTF-8";
    
public final static char hexDigits[]={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'}; 
    /**
     * @param data
     * @return
     * @throws Exception
     * @description BASE64编码
     * @author zikc
     * @date 2015年9月10日 下午2:27:21
     * @update 2015年9月10日 下午2:27:21
     * @version V1.0
     */
    public static String encryptBASE64(String data) throws Exception {
        return (new BASE64Encoder()).encodeBuffer(data.getBytes(UTF8)).trim();
    }

    /**
     * @param data
     * @return
     * @throws Exception
     * @description MD5编码
     * @author zikc
     * @date 2015年9月10日 下午2:28:33
     * @update 2015年9月10日 下午2:28:33
     * @version V1.0
     */
    public static String encryptMD5(String data) throws Exception {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(data.getBytes(UTF8));
        byte[] b = md5.digest();
        // 把密文转换成十六进制的字符串形式
        int j = b.length;
        char str[] = new char[j * 2];
        int k = 0;
        for (int i = 0; i < j; i++) {
            byte byte0 = b[i];
            str[k++] = hexDigits[byte0 >>> 4 & 0xf];
            str[k++] = hexDigits[byte0 & 0xf];
        }
        return new String(str);
    }

    /**
     * @param partner 合作方代码
     * @param dataTime 请求接口的时间
     * @param data 数据内容
     * @param pass 密码
     * @return
     * @throws Exception
     * @description 摘要生成
     * @author zikc
     * @date 2015年9月10日 下午2:29:35
     * @update 2015年9月10日 下午2:29:35
     * @version V1.0
     */
    public static String digest(String partner,String dataTime,String data,String pass) throws Exception {
        return encryptMD5((partner+dataTime+data+pass));
    }
    
}
