package com.gateway.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.util.*;

public class SignParamsUtils {

    private static Logger LOGGER = LoggerFactory.getLogger(SignParamsUtils.class);

    /**
     * 验证消息是否合法
     * @param params 通知返回来的参数数组
     * @return 验证结果
     */
    public static boolean verify(Map<String, String> params, String appSecret) {

        String sign = "";
        if(params.get("sign") != null) {
            sign = params.get("sign");
        }

        String appKey = params.get("key") == null ? "": params.get("key").toString();

        boolean isSign = getSignVeryfy(params, sign, appSecret);

        if (isSign) {
            return true;
        } else {
            return false;
        }
    }


    private static boolean getSignVeryfy(Map<String, String> Params, String sign, String appSecret) {
        //过滤空值、sign参数
        Map<String, String> sParaNew = paraFilter(Params);
        //获取待签名字符串
        String preSignStr = createLinkString(sParaNew, appSecret);
        LOGGER.info(">> sort params is {}", preSignStr);
        //获得签名验证结果
        return verifySign(preSignStr, sign);
    }

    /**
     * 校验签名
     * @param preSignStr 待签名字符串
     * @param sign 待验证签名
     * @return
     */
    public static boolean verifySign(String preSignStr, String sign){
        //指定为UTF-8编码
        String signCurrent = MD5(preSignStr, "UTF-8").toUpperCase();
        LOGGER.info(">> verifySign signCurrent is {}, sign is {}", new Object[]{signCurrent, sign});
        return sign.equals(signCurrent);
    }

    /**
     * 除去数组中的空值和签名参数
     * @param sArray 签名参数组
     * @return 去掉空值与签名参数后的新签名参数组
     */
    public static Map<String, String> paraFilter(Map<String, String> sArray) {

        Map<String, String> result = new HashMap<String, String>();

        if (sArray == null || sArray.size() <= 0) {
            return result;
        }

        for (String key : sArray.keySet()) {
            String value = sArray.get(key);
            if (value == null || value.equals("") || key.equalsIgnoreCase("sign")) {
                continue;
            }
            result.put(key, value);
        }

        return result;
    }

    /**
     * 把数组所有元素排序，并按照“参数=参数值”的模式用“&”字符拼接成字符串
     * @param params 需要排序并参与字符拼接的参数组
     * @return 拼接后字符串
     */
    public static String createLinkString(Map<String, String> params, String appSecret) {

        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);

        String prestr = "";

        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key);

            if (i == keys.size() - 1) {//拼接时，不包括最后一个&字符
                prestr = prestr + key + "=" + value;
            } else {
                prestr = prestr + key + "=" + value + "&";
            }
        }

        prestr = prestr + "&secret=" + appSecret;

        return prestr;
    }

    public final static String MD5(String s, String encode) {
        char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f' };
        try {
            byte[] btInput = s.getBytes(encode);
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            return null;
        }
    }

}
