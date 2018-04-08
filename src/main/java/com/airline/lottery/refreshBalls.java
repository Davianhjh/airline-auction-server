package com.airline.lottery;

import com.airline.tools.*;
import com.alibaba.fastjson.JSON;
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

@Path("/lottery/refreshBalls")
public class refreshBalls {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public refreshBallsRes refreshBalls (@Context HttpHeaders hh, refreshBallsParam rb) {
        MultivaluedMap<String, String> header = hh.getRequestHeaders();
        String AgiToken = header.getFirst("token");
        refreshBallsRes res = new refreshBallsRes();
        Connection conn;
        PreparedStatement pst;
        ResultSet ret;
        boolean verifyResult = verifyRefreshBallsParam(rb);
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
                auctionInfo ai = getAuctionUtil.getAuctionStatus(rb.getAuctionID());
                if (ai != null && ai.getAuctionState() != null && ai.getAuctionType().equals("l") && (ai.getAuctionState().equals("active") || ai.getAuctionState().equals("result"))) {
                    JSONObject response = getBalls(rb.getAuctionID(), rb.getCertificateNo(), uid);
                    if (response != null) {
                        res.setAuth(1);
                        res.setCode(0);
                        res.setAuctionState(ai.getAuctionState());
                        res.setEndCountDown(ai.getEndCountDown());
                        res.setBalls(response.getJSONArray("tickets"));
                        return res;
                    } else {
                        res.setAuth(-2);
                        res.setCode(1060);                    // auction server error
                        return res;
                    }
                } else {
                    res.setAuth(-1);
                    res.setCode(1030);                       // error auctionState
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

    private boolean verifyRefreshBallsParam (refreshBallsParam rb) {
        try {
            return (rb.getAuctionID() != null && rb.getCertificateNo() != null);
        } catch (RuntimeException e) {
            return false;
        }
    }

    public static JSONObject getBalls (String auctionID, String certificateNo, int uid) {
        try {
            Properties serverProp = new Properties();
            InputStream in = refreshBalls.class.getResourceAsStream("/serverAddress.properties");
            serverProp.load(in);
            in.close();
            JSONObject body = new JSONObject();
            String urlStr =  serverProp.getProperty("auctionServiceServer") + "/auction/lottery";
            body.put("auction", auctionID);
            body.put("uid", uid);
            body.put("passenger", certificateNo);
            return httpRequestUtil.postRequest(urlStr, null, body);
        } catch (Exception e){
            e.printStackTrace();
            return null;                        // request failed OR bad response
        }
    }
}
