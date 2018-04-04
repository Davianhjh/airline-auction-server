package com.test;

import com.airline.baseAuctionData;
import com.airline.tools.httpRequestUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.servlet.http.HttpServletRequest;

@Path("/test")
public class restfulTest {
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String printTesting(@Context HttpServletRequest request) {
        /*
        MultivaluedMap<String, String> body = form.asMap();
        System.out.println(body.size());
        System.out.println(body.getFirst("key1"));
        System.out.println(body.getFirst("key2"));
        Map<String, String> params = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : body.entrySet()) {
            System.out.println(entry.getKey());
            System.out.println(entry.getValue().get(0));
        }
        */
        /*
        String urlPath = "http://192.168.1.233:9000/auction/flights";
        ArrayList<String> flightArray = new ArrayList<String>();
        flightArray.add("2018-02-26-MU5186");
        JSONObject body = new JSONObject();
        body.put("flights", flightArray);
        JSONObject response = httpRequestUtil.postRequest(urlPath, null, body);
        for(String flightID: flightArray){
            JSONArray fa = response.getJSONArray(flightID);
            if(fa != null){
                System.out.println(flightID + ":");
                System.out.println(fa.toJSONString());
                for(int i=0; i<fa.size(); i++){
                    JSONObject auction = fa.getJSONObject(i);
                    System.out.println(auction.getString("id"));
                    System.out.println(auction.getString("type"));
                    System.out.println(auction.getString("status"));
                    System.out.println(auction.getBigInteger("start"));
                    System.out.println(auction.getBigInteger("end"));
                    System.out.println(auction.getIntValue("startCountDown"));
                    System.out.println(auction.getIntValue("endCountDown"));
                    System.out.println(auction.getString("description"));
                    System.out.println("*************************************");
                }
                System.out.println();
                System.out.println();
            }
        }
        System.out.println("Default Charset=" + Charset.defaultCharset());
        System.out.println("file.encoding=" + System.getProperty("file.encoding"));
        System.out.println("Default Charset in Use=" + getDefaultCharSet());
        */
        String ip = request.getRemoteAddr();
        return "Testing Jersey Restful API! Your IP is: " + ip;
    }

    @GET
    @Path("/getUser")
    @Produces(MediaType.APPLICATION_JSON)
    public Response printUser(@QueryParam("username") String username, @QueryParam("id") int i) {
        User user = new User();
        user.setId(i);
        user.setName(username);
        Response response = Response.status(200).
                entity(user).
                header("Access-Control-Allow-Origin", "*").build();
        return response;
    }

    @POST
    @Path("/postUser")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public int sendUser(@Context HttpServletRequest request, @Context HttpHeaders hh, User user) {
        MultivaluedMap<String, String> header = hh.getRequestHeaders();
        String token = header.getFirst("token");
        Map<String,Cookie> tmp = hh.getCookies();
        for(Cookie item : tmp.values()) {
            System.out.println(item.getValue());
        }
        System.out.println(user.getId());
        System.out.println(user.getName());
        System.out.println(token);
        return 0;
    }

    private static String getDefaultCharSet(){
        OutputStreamWriter writer = new OutputStreamWriter(new ByteArrayOutputStream());
        String enc = writer.getEncoding();
        return enc;
    }
}
