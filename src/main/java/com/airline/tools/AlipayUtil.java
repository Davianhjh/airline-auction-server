package com.airline.tools;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class AlipayUtil {

    public static String alipayStr (String outTradeNo, String body, String subject, String totalAmount) {
        try {
            Properties property = new Properties();
            InputStream in = AlipayUtil.class.getResourceAsStream("/paymentManage.properties");
            property.load(in);
            String APP_ID = property.getProperty("appid");
            String APP_PRIVATE_KEY = RSAPrivateKeyFormat(readRSAKey("AgiviewKey"));
            String ALIPAY_PUBLIC_KEY = RSAPublicKeyFormat(readRSAKey("AlipayPub"));
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
                request.setNotifyUrl("http://220.202.15.42:10020/auction/receive_notify");
                AlipayTradeAppPayResponse response = alipayClient.sdkExecute(request);
                return response.getBody();
            }
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private static String readRSAKey (String keyName) {
        try {
            Properties property = new Properties();
            InputStream in = AlipayUtil.class.getResourceAsStream("/paymentManage.properties");
            property.load(in);
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

}
