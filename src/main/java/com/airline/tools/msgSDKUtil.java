package com.airline.tools;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;

public class msgSDKUtil {
    /************
     * 获取时间戳
     *
     * @return
     */
    public static String getTimestamp() {
        return new SimpleDateFormat("MMddHHmmss").format(new Date());
    }

    /************
     * 加密处理
     *
     * @param userName
     * @param password
     * @param timestemp
     * @return
     */
    public static String getKey(String userName, String password, String timestemp) {
        String key = "";
        try {
            MessageDigest mdTemp = MessageDigest.getInstance("MD5");
            mdTemp.update(userName.getBytes());
            mdTemp.update(password.getBytes());
            mdTemp.update(timestemp.getBytes());
            key = bytesToHexString(mdTemp.digest());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return key;
    }

    /**********
     * 字节数组转16进制字符串
     *
     * @param src
     * @return
     */
    public static String bytesToHexString(byte src[]) {
        String resultString = "";
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0)
            return null;
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 255;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2)
                stringBuilder.append(0);
            stringBuilder.append(hv);
        }

        resultString = stringBuilder.toString();
        return resultString;
    }

    /**
     * POST发送消息
     *
     * @param Content 发送的内容
     * @param _Header HTTP头信息
     * @return HTTPResponse应答
     */
    public static InputStream SendMessage(byte[] Content, Hashtable _Header, String serverAddress) {
        return SendMessage(Content, _Header, serverAddress, null, 0, null, null);
    }

    public static InputStream SendMessage(byte[] Content, Hashtable _Header, String serverAddress, String proxyHost, int proxyPort, String userName, String password) {
        InputStream _InputStream = null;

        try {
            if (serverAddress == null)
                return null;

            HttpPost _HttpPost = new HttpPost(serverAddress);
            if (_Header != null) {
                Enumeration _List = _Header.keys();
                String keyString = "";
                try {
                    while (_List.hasMoreElements()) {
                        keyString = _List.nextElement().toString();
                        _HttpPost.addHeader(keyString, _Header.get(keyString).toString());
                    }
                } catch (NoSuchElementException e) {
                    e.printStackTrace();
                }
            }
            _HttpPost.addHeader("Connection", "Keep-Alive");
            _HttpPost.setEntity(new ByteArrayEntity(Content));
            HttpClient _HttpClient = new DefaultHttpClient();
            HttpResponse _HttpResponse = _HttpClient.execute(_HttpPost);

            if (_Header != null) {
                _Header.put("ResponseContentLength", String.valueOf(_HttpResponse.getEntity().getContentLength()));
                Header[] _RespHeader = _HttpResponse.getAllHeaders();
                if (_RespHeader != null && _RespHeader.length > 0) {
                    for (Header header : _RespHeader) {
                        _Header.put(header.getName(), header.getValue());
                    }
                }
            }
            _InputStream = _HttpResponse.getEntity().getContent();
        } catch (Exception e) {
            System.out.println("请求失败，原因：" + e.getMessage() + "请求地址：" + serverAddress);
        }
        return _InputStream;
    }

    /**
     * 解析返回的流
     *
     * @param _InputStream
     * @return
     */

    public static String getResponseCode(InputStream _InputStream, String Charset) {
        try {
            if (_InputStream != null) {
                StringBuffer buffer = new StringBuffer();
                String line = null;
                BufferedReader br = new BufferedReader(new InputStreamReader(_InputStream));
                while ((line = br.readLine()) != null) {
                    buffer.append(line);
                }
                return buffer.toString();

            } else {
                return "error";
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("获取响应错误，原因：" + e.getMessage());
            return "error";
        }
    }
}
