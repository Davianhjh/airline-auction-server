package com.airline.lottery;

import com.airline.tools.HiKariCPHandler;
import com.airline.tools.UTCTimeUtil;
import com.airline.tools.httpRequestUtil;
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
import java.util.Properties;

@Path("/lottery/deliverBalls")
public class deliverBalls {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public deliverBallsRes deliver (@Context HttpHeaders hh, JSONObject testBody) {
        MultivaluedMap<String, String> header = hh.getRequestHeaders();
        String AgiToken = header.getFirst("token");
        deliverBallsRes res = new deliverBallsRes();
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
                if (transactionID != null) {
                    String updateSql = "UPDATE ballTransaction set paymentState=1 WHERE transactionNo=?;";
                    pst = conn.prepareStatement(updateSql);
                    pst.setString(1, transactionID);
                    pst.executeUpdate();
                }
                else transactionID = testBody.getJSONObject("alipay_trade_app_pay_response").getString("out_trade_no");
                String searchSql = "SELECT auctionID, certificateNo, uid, quantity FROM ballTransaction WHERE transactionNo=? AND paymentState=1";
                pst = conn.prepareStatement(searchSql);
                pst.setString(1, transactionID);
                ret2 = pst.executeQuery();
                if (ret2.next()) {
                    JSONObject response = getBallsFromServer(ret2.getString(1), ret2.getString(2), ret2.getInt(3), ret2.getInt(4));
                    if (response != null) {
                        if (response.getBoolean("created")) {
                            res.setAuth(1);
                            res.setCode(0);
                            res.setVerify(1);
                            return res;
                        } else {
                            res.setAuth(-2);
                            res.setCode(1061);                          // ball deliver error
                            return res;
                        }
                    } else {
                        res.setAuth(-2);
                        res.setCode(1060);                              // auction server error
                        return res;
                    }
                } else {
                    res.setAuth(-1);
                    res.setCode(1033);                                  // payment not verified
                    return res;
                }
            } else {
                res.setAuth(-1);
                res.setCode(1020);                                      // user not found
                return res;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            res.setAuth(-2);
            res.setCode(2000);                                          // mysql error
            return res;
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private JSONObject getBallsFromServer (String auctionID, String certificateNo, int uid, int quantity) {
        try {
            Properties serverProp = new Properties();
            InputStream in = deliverBalls.class.getResourceAsStream("/serverAddress.properties");
            serverProp.load(in);
            in.close();
            JSONObject body = new JSONObject();
            String urlStr =  serverProp.getProperty("auctionServiceServer") + "/auction/gamble";
            body.put("auction", auctionID);
            body.put("uid", uid);
            body.put("passenger", certificateNo);
            body.put("quantity", quantity);
            return httpRequestUtil.postRequest(urlStr, null, body);
        } catch (Exception e){
            e.printStackTrace();
            return null;                        // request failed OR bad response
        }
    }
}
