package com.airline.tools;

import com.alibaba.fastjson.JSONObject;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;

public class httpRequestUtil {

    public static JSONObject postRequest(String urlPath, String token, JSONObject body) throws Exception {
        URI uri = new URI(urlPath);
        Client postClient = jerseyClientFactory.create();
        WebTarget webTarget = postClient.target(uri);
        Response response = webTarget.request(MediaType.APPLICATION_JSON_TYPE).header("token", token).post(Entity.entity(body, MediaType.APPLICATION_JSON_TYPE));
        JSONObject res =  response.readEntity(JSONObject.class);
        return res;
        /*
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
            byte[] data = new byte[in.available()];
            in.read(data);
            res = new String(data);
            String errMsg = "error";
            if(res.contains(errMsg))
                throw new Exception();
            return res;
        }
        else {
            System.out.println("Request Error: " + conn.getResponseCode());
            throw new Exception();
        }
        */
    }
}
