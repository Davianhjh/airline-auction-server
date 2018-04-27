package com.airline.tools;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Hashtable;

public class msgSendUtil {

    public static int sendMsg (String tel, String body) {
        try {
            String UserName="wbsp";
            String Password="542010";
            String Timestamp=msgSDKUtil.getTimestamp();
            String Key=msgSDKUtil.getKey(UserName, Password, Timestamp);
            String serverAddress = "http://www.youxinyun.com:3070/Platform_Http_Service/servlet/SendSms";
            Long SMSID = System.currentTimeMillis();
            StringBuffer _StringBuffer = new StringBuffer();
            _StringBuffer.append("UserName=" + UserName + "&");
            _StringBuffer.append("Key=" + Key + "&");
            _StringBuffer.append("Timestemp=" + Timestamp + "&");
            _StringBuffer.append("Content=" + URLEncoder.encode(body, "utf-8") + "&");
            _StringBuffer.append("CharSet=utf-8&");
            _StringBuffer.append("Mobiles=" + tel + "&");
            _StringBuffer.append("SMSID=" + SMSID);
            Hashtable _Header = new Hashtable();
            _Header.put("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
            InputStream _InputStream = msgSDKUtil.SendMessage(_StringBuffer.toString().getBytes("utf-8"), _Header, serverAddress);
            String response = msgSDKUtil.getResponseCode(_InputStream, "utf-8");
            if (response.equals("error"))
                return -1;
            JSONObject res = JSON.parseObject(response);
            return res.getIntValue("result");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
