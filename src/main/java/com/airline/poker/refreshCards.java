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

@Path("/refreshCards")
public class refreshCards {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public refreshCardsRes refresh (@Context HttpHeaders hh, refreshCardsParam fc) {
        MultivaluedMap<String, String> header = hh.getRequestHeaders();
        String AgiToken = header.getFirst("token");
        refreshCardsRes res = new refreshCardsRes();
        Connection conn;
        PreparedStatement pst;
        ResultSet ret, ret2;
        boolean verifyResult = verifyRefreshCardsParam(fc);
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
                auctionInfo ai = getAuctionUtil.getAuctionStatus(fc.getAuctionID());
                if (ai != null && ai.getAuctionType().equals("p")) {
                    JSONObject response = getCards(fc.getAuctionID(), fc.getCertificateNo(), uid);
                    if (response != null) {
                        String searchSql = "SELECT COUNT(tradeID) FROM cardTransaction WHERE auctionID=? AND paymentState=1;";
                        pst = conn.prepareStatement(searchSql);
                        pst.setString(1, fc.getAuctionID());
                        ret2 = pst.executeQuery();
                        ret2.next();

                        res.setAuth(1);
                        res.setCode(0);
                        res.setAuctionState(ai.getAuctionState());
                        res.setEndCountDown(response.getIntValue("endCountDown"));
                        res.setTotalAmount(ret2.getInt(1));
                        JSONArray cards = response.getJSONArray("cards");
                        ArrayList<card> cardArray = new ArrayList<card>();
                        for (int i = 0; i < cards.size(); i++) {
                            card tmp = new card();
                            tmp.setSuit(cards.getJSONObject(i).getString("suit"));
                            tmp.setNumber(cards.getJSONObject(i).getString("number"));
                            cardArray.add(tmp);
                        }
                        res.setCards(cardArray);
                        cardArray.clear();
                        return res;
                    } else {
                        res.setAuth(-2);
                        res.setCode(1060);                    // auction server error
                        return res;
                    }
                } else {
                    res.setAuth(-2);
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

    private boolean verifyRefreshCardsParam (refreshCardsParam fc) {
        try {
            return (fc.getAuctionID() != null && fc.getCertificateNo() != null);
        } catch (RuntimeException e) {
            return false;
        }
    }

    private JSONObject getCards (String auctionID, String certificateNo, int uid) {
        try {
            Properties serverProp = new Properties();
            InputStream in = refreshCards.class.getResourceAsStream("/serverAddress.properties");
            serverProp.load(in);
            in.close();
            JSONObject body = new JSONObject();
            String urlStr =  serverProp.getProperty("auctionServiceServer") + "/poker/cards";
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
