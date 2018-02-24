package com.airline.tools;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class httpRequestUtil {

    public static String postRequest(String urlPath, String token, String body){
        try {
            URL url = new URL(urlPath);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestProperty("accept", "application/json");
            if(token != null)
                conn.setRequestProperty("token", token);
            if(body != null)
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream(),"UTF-8");
            out.append(body);
            out.flush();
            out.close();
            if(conn.getResponseCode() == 200){
                InputStream in = conn.getInputStream();
                String res = null;
                try {
                    byte[] data = new byte[in.available()];
                    in.read(data);
                    res = new String(data);
                    return res;
                } catch (Exception x){
                    x.printStackTrace();
                    return null;
                }
            }
            else {
                System.out.println("Request Error: " + conn.getResponseCode());
                return null;
            }
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
