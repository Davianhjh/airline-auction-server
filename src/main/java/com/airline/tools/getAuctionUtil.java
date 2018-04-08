package com.airline.tools;

import com.airline.auction.passengerResult;
import com.alibaba.fastjson.JSONObject;

import java.io.InputStream;
import java.util.Properties;

public class getAuctionUtil {

    public static auctionInfo getAuctionStatus (String auctionID) {
        JSONObject response;
        Properties serverProp = new Properties();
        try {
            InputStream in = getAuctionUtil.class.getResourceAsStream("/serverAddress.properties");
            serverProp.load(in);
            in.close();
            String urlStr = serverProp.getProperty("auctionServiceServer") + "/auction/status";
            JSONObject body = new JSONObject();
            body.put("auction", auctionID);
            response = httpRequestUtil.postRequest(urlStr, null, body);
        } catch (Exception e){
            e.printStackTrace();
            return null;                                 // request failed OR bad response
        }
        auctionInfo auctionInfo = new auctionInfo();
        auctionInfo.setAuctionState(response.getString("status"));
        auctionInfo.setAuctionType(response.getString("type"));
        auctionInfo.setStartCountDown(response.getIntValue("startCountDown"));
        auctionInfo.setEndCountDown(response.getIntValue("endCountDown"));
        auctionInfo.setStartTime(response.getLongValue("start"));
        auctionInfo.setEndTime(response.getLongValue("end"));
        auctionInfo.setDescription(response.getString("description"));
        return auctionInfo;
    }

    public static boolean bidForPrice (int uid, String auctionID, String certificateNo, double price) {
        JSONObject response;
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
            response = httpRequestUtil.postRequest(urlStr, null, body);
        } catch (Exception e) {
            e.printStackTrace();
            return false;                                // request failed OR bad response
        }
        return response.getString("error") == null;
    }

    public static passengerResult getBiddingResult (int uid, String auctionID, String certificateNo) throws Exception {
        JSONObject response;
        Properties serverProp = new Properties();
        InputStream in = getAuctionUtil.class.getResourceAsStream("/serverAddress.properties");
        serverProp.load(in);
        in.close();
        String urlStr = serverProp.getProperty("auctionServiceServer") + "/auction/passenger_result";
        JSONObject body = new JSONObject();
        body.put("auction", auctionID);
        body.put("uid", uid);
        body.put("passenger", certificateNo);
        response = httpRequestUtil.postRequest(urlStr, null, body);
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
