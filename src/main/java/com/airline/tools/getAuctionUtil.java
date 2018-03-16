package com.airline.tools;

import com.airline.auction.passengerResult;
import com.alibaba.fastjson.JSONObject;

import java.io.InputStream;
import java.util.Properties;

public class getAuctionUtil {

    public static auctionInfo getAuctionStatus (String auctionID) {
        String resStr;
        Properties serverProp = new Properties();
        try {
            InputStream in = getAuctionUtil.class.getResourceAsStream("/serverAddress.properties");
            serverProp.load(in);
            in.close();
            String urlStr = serverProp.getProperty("auctionServiceServer") + "/auction/status";
            JSONObject body = new JSONObject();
            body.put("auction", auctionID);
            resStr = httpRequestUtil.postRequest(urlStr, null, body.toJSONString());
        } catch (Exception e){
            e.printStackTrace();
            return null;                                 // request failed OR bad response
        }
        JSONObject response = JSONObject.parseObject(resStr);
        auctionInfo auctionInfo = new auctionInfo();
        auctionInfo.setAuctionState(response.getString("status"));
        auctionInfo.setAuctionType(response.getString("type"));
        return auctionInfo;
    }

    public static boolean bidForPrice (int uid, String auctionID, String certificateNo, double price) {
        String resStr;
        Properties serverProp = new Properties();
        try {
            InputStream in = getAuctionUtil.class.getResourceAsStream("/serverAddress.properties");
            serverProp.load(in);
            in.close();
            String urlStr = serverProp.getProperty("auctionServiceServer") + "/auction/bid";
            JSONObject body = new JSONObject();
            body.put("auction", auctionID);
            body.put("uid", uid);
            body.put("passenger", certificateNo);
            body.put("price", price);
            resStr = httpRequestUtil.postRequest(urlStr, null, body.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
            return false;                                // request failed OR bad response
        }
        JSONObject response = JSONObject.parseObject(resStr);
        return response.getString("error") == null;
    }

    public static passengerResult getBiddingResult (int uid, String auctionID, String certificateNo) throws Exception {
        String resStr;
        Properties serverProp = new Properties();
            InputStream in = getAuctionUtil.class.getResourceAsStream("/serverAddress.properties");
            serverProp.load(in);
            in.close();
            String urlStr = serverProp.getProperty("auctionServiceServer") + "/auction/passenger_result";
            JSONObject body = new JSONObject();
            body.put("auction", auctionID);
            body.put("uid", uid);
            body.put("passenger", certificateNo);
            resStr = httpRequestUtil.postRequest(urlStr, null, body.toJSONString());
        JSONObject response = JSONObject.parseObject(resStr);
        passengerResult pRes = new passengerResult();
        pRes.setAuctionState(response.getString("status"));
        pRes.setAuctionType(response.getString("type"));
        pRes.setBiddingPrice(response.getDoubleValue("price") == 0 ? -1:response.getDoubleValue("price"));
        pRes.setBiddingTime(response.getString("time") == null ? "-1":response.getString("time"));
        pRes.setHit(response.getString("hit") == null ? "o":response.getString("hit"));
        pRes.setPaymentPrice(-1);
        if (pRes.getHit().equals("Y")) {
            pRes.setPaymentPrice(pRes.getBiddingPrice());
            if (pRes.getAuctionType().equals("2")) {
                pRes.setPaymentPrice(response.getDoubleValue("type2_price"));
            }
        }
        return pRes;
    }
}
