package com.airline.tools;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public class AlipayAPPUtil {

    public static String alipayStr (String outTradeNo, String body, String subject, String totalAmount, String notify_url) {
        try {
            Properties property = new Properties();
            InputStream in = AlipayAPPUtil.class.getResourceAsStream("/paymentManage.properties");
            property.load(in);
            in.close();
            String APP_ID = property.getProperty("appid");
            String APP_PRIVATE_KEY = readRSAKey("AgiviewKey");
            String ALIPAY_PUBLIC_KEY = readRSAKey("AlipayPub");
            if (APP_ID == null || ALIPAY_PUBLIC_KEY == null || APP_PRIVATE_KEY == null) {
                return null;
            } else {
                AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", APP_ID, APP_PRIVATE_KEY, "json", "utf-8", ALIPAY_PUBLIC_KEY, "RSA2");
                AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
                AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
                model.setBody(body);
                model.setSubject(subject);
                model.setOutTradeNo(outTradeNo);
                model.setTimeoutExpress("30m");
                model.setTotalAmount(totalAmount);
                model.setProductCode("lucky balls");
                request.setBizModel(model);
                request.setNotifyUrl(notify_url);
                AlipayTradeAppPayResponse response = alipayClient.sdkExecute(request);
                return response.getBody();
            }
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static boolean verifyPayment (Map<String, String> params, String charSet) {
        try {
            String ALIPAY_PUBLIC_KEY = readRSAKey("AlipayPub");
            return AlipaySignature.rsaCheckV1(params, ALIPAY_PUBLIC_KEY, charSet, "RSA2");
        } catch (Exception e) {
            return false;
        }
    }

    private static String readRSAKey (String keyName) {
        try {
            Properties property = new Properties();
            InputStream in = AlipayAPPUtil.class.getResourceAsStream("/paymentManage.properties");
            property.load(in);
            in.close();
            File file = new File(property.getProperty(keyName));
            long fileLength = file.length();
            FileInputStream fis = new FileInputStream(file);
            DataInputStream dis = new DataInputStream(fis);
            byte[] keyBytes = new byte[(int) fileLength];
            dis.readFully(keyBytes);
            return new String(keyBytes);
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
/*
    private static String RSAPrivateKeyFormat (String keyStr) {
        if (keyStr == null)
            return null;
        String[] arr = keyStr.split("\n");
        StringBuffer buffer = new StringBuffer(24);
        for (int i=0; i<arr.length; i++) {
            if (i!=0 && i!=arr.length-1) {
                buffer.append(arr[i]);
            }
        }
        return new String (buffer);
    }

    private static String RSAPublicKeyFormat (String keyStr) {
        if (keyStr == null)
            return null;
        String[] arr = keyStr.split("\n");
        StringBuffer buffer = new StringBuffer(24);
        for (int i=0; i<arr.length; i++) {
            if (i!=0 && i!=arr.length-1) {
                buffer.append(arr[i].substring(0, arr[i].length()-1));
            }
        }
        return new String (buffer);
    }
*/
}
