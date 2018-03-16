package com.airline.poker;

import com.airline.tools.HiKariCPHandler;
import com.airline.tools.UTCTimeUtil;
import com.airline.tools.httpRequestUtil;
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

@Path("/deliverCards")
public class deliverCards {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public deliverCardsRes deliver (@Context HttpHeaders hh, JSONObject testBody) {
        MultivaluedMap<String, String> header = hh.getRequestHeaders();
        String AgiToken = header.getFirst("token");
        deliverCardsRes res = new deliverCardsRes();
        Connection conn;
        PreparedStatement pst;
        ResultSet ret, ret2;

        if (AgiToken == null) {
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
                String transactionID = testBody.getString("transactionID");
                if (transactionID == null)
                    transactionID = testBody.getJSONObject("alipay_trade_app_pay_response").getString("out_trade_no");
                String searchSql = "SELECT auctionID, certificateNo, uid, card FROM cardTransaction WHERE transactionNo=? AND paymentState=1";
                pst = conn.prepareStatement(searchSql);
                pst.setString(1, transactionID);
                ret2 = pst.executeQuery();
                if (ret2.next()) {
                    String resStr = getCardsFromServer(ret2.getString(1), ret2.getString(2), ret2.getInt(3), ret2.getString(4).split(",").length);
                    if (resStr != null) {
                        JSONObject response = JSONObject.parseObject(resStr);
                        if (response.getBoolean("success")) {
                            JSONArray existing = response.getJSONArray("existing");
                            JSONArray delivered = response.getJSONArray("new");
                            ArrayList<card> cardArray = new ArrayList<card>();
                            for (int i = 0; i < existing.size(); i++) {
                                card tmp = new card();
                                tmp.setSuit(existing.getJSONObject(i).getString("suit"));
                                tmp.setNumber(existing.getJSONObject(i).getString("number"));
                                cardArray.add(tmp);
                            }
                            res.setExistingCards(cardArray);
                            cardArray.clear();
                            for (int i = 0; i < delivered.size(); i++) {
                                card tmp = new card();
                                tmp.setSuit(delivered.getJSONObject(i).getString("suit"));
                                tmp.setNumber(delivered.getJSONObject(i).getString("number"));
                                cardArray.add(tmp);
                            }
                            res.setNewCards(cardArray);
                            cardArray.clear();
                            conn.close();
                            res.setAuth(1);
                            res.setCode(0);
                            res.setVerify(1);
                            return res;
                        } else {
                            conn.close();
                            res.setAuth(-2);
                            res.setCode(1061);                               // card exceeded
                            return res;
                        }
                    } else {
                        conn.close();
                        res.setAuth(-2);
                        res.setCode(1060);                                   // auction server error
                        return res;
                    }
                } else {
                    conn.close();
                    res.setAuth(-1);
                    res.setCode(1033);                                  // payment not verified
                    return res;
                }
            } else {
                conn.close();
                res.setAuth(-1);
                res.setCode(1020);                                      // user not found
                return res;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            res.setAuth(-2);
            res.setCode(2000);                                          // mysql error
            return res;
        }
    }

    private String getCardsFromServer(String auctionID, String certificateNo, int uid, int cardNumber) {
        try {
            Properties serverProp = new Properties();
            InputStream in = deliverCards.class.getResourceAsStream("/serverAddress.properties");
            serverProp.load(in);
            in.close();
            JSONObject body = new JSONObject();
            String urlStr =  serverProp.getProperty("auctionServiceServer") + "/poker/request_card";
            body.put("auction", auctionID);
            body.put("uid", uid);
            body.put("passenger", certificateNo);
            body.put("quantity", cardNumber);
            return httpRequestUtil.postRequest(urlStr, null, body.toJSONString());
        } catch (Exception e){
            e.printStackTrace();
            return null;                        // request failed OR bad response
        }
    }
}
