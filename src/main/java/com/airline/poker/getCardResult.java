package com.airline.poker;

import com.airline.tools.*;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

@Path("/getCardResult")
public class getCardResult {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public getCardResultRes getResult (@Context HttpHeaders hh, getCardResultParam cr) {
        MultivaluedMap<String, String> header = hh.getRequestHeaders();
        String AgiToken = header.getFirst("token");
        getCardResultRes res = new getCardResultRes();
        Connection conn;
        PreparedStatement pst;
        ResultSet ret;
        boolean verifyResult = verifyCardResultParam(cr);
        if (AgiToken == null || !verifyResult) {
            res.setAuth(-1);
            res.setCode(1000);                               // parameters not correct
            return res;
        }

        try {
            conn = HiKariCPHandler.getConn();
        } catch (SQLException e){
            e.printStackTrace();
            res.setAuth(-2);
            res.setCode(2000);                               // fail to get mysql connection
            return res;
        }
        try {
            String utcTimeStr = UTCTimeUtil.getUTCTimeStr();
            String verifySql = "SELECT id FROM customerToken INNER JOIN customerAccount ON customerToken.uid = customerAccount.id WHERE token = ? and expire > ?;";
            pst = conn.prepareStatement(verifySql);
            pst.setString(1, AgiToken);
            pst.setString(2, utcTimeStr);
            ret = pst.executeQuery();
            if (ret.next()) {
                int uid = ret.getInt(1);
                auctionInfo ai = getAuctionUtil.getAuctionStatus(cr.getAuctionID());
                if (ai != null && ai.getAuctionState() != null && ai.getAuctionState().equals("result") && ai.getAuctionType().equals("p")) {
                    JSONObject response = getPokerResult(cr.getAuctionID(), cr.getCertificateNo(), uid);
                    if (response != null) {
                        res.setAuth(1);
                        res.setCode(0);
                        res.setHit(response.getBoolean("win") ? 1:0);
                        res.setUserCards(response.getJSONArray("cards"));
                        res.setWinner(response.getJSONArray("winner"));
                        return res;
                    } else {
                        res.setAuth(-2);
                        res.setCode(1060);                   // auction server error
                        return res;
                    }
                } else {
                    res.setAuth(-1);
                    res.setCode(1030);                       // error auction status
                    return res;
                }
            } else {
                res.setAuth(-1);
                res.setCode(1020);                           // user not found
                return res;
            }
        } catch (SQLException e){
            e.printStackTrace();
            res.setAuth(-2);
            res.setCode(2000);                               // mysql error
            return res;
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean verifyCardResultParam(getCardResultParam cr) {
        try {
            return (cr.getAuctionID() != null && cr.getCertificateNo() != null);
        } catch (RuntimeException e) {
            return false;
        }
    }

    public static JSONObject getPokerResult(String auctionID, String certificateNo, int uid) {
        try {
            Properties serverProp = new Properties();
            InputStream in = getCardResult.class.getResourceAsStream("/serverAddress.properties");
            serverProp.load(in);
            in.close();
            JSONObject body = new JSONObject();
            String urlStr =  serverProp.getProperty("auctionServiceServer") + "/poker/user_result";
            body.put("auction", auctionID);
            body.put("uid", uid);
            body.put("passenger", certificateNo);
            return httpRequestUtil.postRequest(urlStr, null, body);
        } catch (Exception e){
            e.printStackTrace();
            return null;                                     // request failed OR bad response
        }
    }
}
