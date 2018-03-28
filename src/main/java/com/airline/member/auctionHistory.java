package com.airline.member;

import com.airline.baseUserAuctionData;
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

@Path("/auctionHistory")
public class auctionHistory {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public auctionHistoryRes history (@Context HttpHeaders hh, JSONObject body) {
        MultivaluedMap<String, String> header = hh.getRequestHeaders();
        String AgiToken = header.getFirst("token");
        auctionHistoryRes res = new auctionHistoryRes();
        Connection conn;
        PreparedStatement pst, pst2;
        ResultSet ret,ret2;
        int dayLap;
        if (AgiToken == null) {
            res.setAuth(-1);
            res.setCode(1000);                               // parameters not correct
            return res;
        }
        try {
            dayLap = body.getIntValue("day");
        } catch (NullPointerException e) {
            dayLap = 30;
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
                JSONObject response = getUserAuctionHistory(uid, dayLap);
                if (response != null) {
                    JSONArray arr = response.getJSONArray("history");
                    ArrayList<baseUserAuctionData> history = new ArrayList<>();
                    for(int i=0; i<arr.size(); i++) {
                        JSONObject tmp = arr.getJSONObject(i);
                        if (tmp.getString("type").equals("1") || tmp.getString("type").equals("2")) {
                            String tmp_sql = "SELECT passengerName, paymentState FROM (SELECT certificateNo, paymentState FROM tradeRecord WHERE uid=? AND " +
                                    "certificateNo=? AND auctionID=?) as tr RIGHT JOIN passengerFlight ON tr.certificateNo=passengerFlight.certificateNo WHERE " +
                                    "passengerFlight.flightNo=? AND passengerFlight.flightDate=? AND passengerFlight.certificateNo=?";
                            PreparedStatement tmp_pst = conn.prepareStatement(tmp_sql);
                            tmp_pst.setInt(1, uid);
                            tmp_pst.setString(2, tmp.getString("passenger"));
                            tmp_pst.setString(3, tmp.getString("auction"));
                            tmp_pst.setString(4, tmp.getString("flight").substring(11));
                            tmp_pst.setString(5, tmp.getString("flight").substring(0,10));
                            tmp_pst.setString(6, tmp.getString("passenger"));
                            ResultSet tmp_ret = tmp_pst.executeQuery();
                            while (tmp_ret.next()) {
                                baseUserAuctionData tmp_Data = new baseUserAuctionData();
                                tmp_Data.setFlightNo(tmp.getString("flight").substring(11));
                                tmp_Data.setFlightDate(tmp.getString("flight").substring(0,10));
                                tmp_Data.setPassengerName(tmp_ret.getString(1));
                                tmp_Data.setCertificateNo(tmp.getString("passenger"));
                                tmp_Data.setAuctionID(tmp.getString("auction"));
                                tmp_Data.setAuctionType(tmp.getString("type"));
                                tmp_Data.setAuctionState(tmp.getString("status"));
                                tmp_Data.setStartTime(tmp.getLongValue("start"));
                                tmp_Data.setEndTime(tmp.getLongValue("end"));
                                tmp_Data.setDescription(tmp.getString("description"));
                                tmp_Data.setBiddingPrice(tmp.getDoubleValue("price"));
                                tmp_Data.setBiddingTime(tmp.getLongValue("time"));
                                tmp_Data.setHit(tmp.getString("hit"));
                                tmp_Data.setPaymentState(tmp_ret.getInt(2));
                                history.add(tmp_Data);
                            }
                            tmp_ret.close();
                            tmp_pst.close();
                        } else {
                            String sql2 = "SELECT passengerName FROM passengerFlight WHERE flightNo=? AND flightDate=? AND certificateNo=?";
                            pst2 = conn.prepareStatement(sql2);
                            pst2.setString(1, tmp.getString("flight").substring(11));
                            pst2.setString(2, tmp.getString("flight").substring(0,10));
                            pst2.setString(3, tmp.getString("passenger"));
                            ret2 = pst2.executeQuery();
                            if (ret2.next()) {
                                baseUserAuctionData tmp_Data = new baseUserAuctionData();
                                tmp_Data.setFlightNo(tmp.getString("flight").substring(11));
                                tmp_Data.setFlightDate(tmp.getString("flight").substring(0,10));
                                tmp_Data.setPassengerName(ret2.getString(1));
                                tmp_Data.setCertificateNo(tmp.getString("passenger"));
                                tmp_Data.setAuctionID(tmp.getString("auction"));
                                tmp_Data.setAuctionType(tmp.getString("type"));
                                tmp_Data.setAuctionState(tmp.getString("status"));
                                tmp_Data.setStartTime(tmp.getLongValue("start"));
                                tmp_Data.setEndTime(tmp.getLongValue("end"));
                                tmp_Data.setDescription(tmp.getString("description"));
                                tmp_Data.setBiddingPrice(tmp.getDoubleValue("price"));
                                tmp_Data.setBiddingTime(tmp.getLongValue("time"));
                                tmp_Data.setHit(tmp.getString("hit"));
                                tmp_Data.setPaymentState(1);
                                history.add(tmp_Data);
                            } else {
                                res.setAuth(-1);
                                res.setCode(1040);           // user ticket info not found
                                return res;
                            }
                        }
                    }
                    res.setAuth(1);
                    res.setCode(0);
                    res.setHistory(history);
                    return res;
                } else {
                    res.setAuth(-2);
                    res.setCode(1060);                       // auction server error
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

    private JSONObject getUserAuctionHistory (int uid, int dayLap) {
        try {
            Properties serverProp = new Properties();
            InputStream in = auctionHistory.class.getResourceAsStream("/serverAddress.properties");
            serverProp.load(in);
            in.close();
            JSONObject body = new JSONObject();
            String urlStr =  serverProp.getProperty("auctionServiceServer") + "/auction/user_all";
            body.put("uid", uid);
            body.put("time_span", dayLap);
            return httpRequestUtil.postRequest(urlStr, null, body);
        } catch (Exception e){
            e.printStackTrace();
            return null;                                     // request failed OR bad response
        }
    }
}
