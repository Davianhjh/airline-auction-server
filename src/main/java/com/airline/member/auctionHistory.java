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
                    for (int i=0; i<arr.size(); i++) {
                        baseUserAuctionData tmp = new baseUserAuctionData();
                        JSONObject tmpData = arr.getJSONObject(i);
                        String flightID = tmpData.getString("flight");
                        String certificateNo = tmpData.getString("passenger");
                        String sql = "SELECT passengerName, mobile, certificateNo, carbinClass, dptAirport, dptAptCode, arvAirport, arvAptCode, depTime, arrTime FROM passengerFlight WHERE certificateNo=? AND flightNo=? AND flightDate=?";
                        pst2 = conn.prepareStatement(sql);
                        pst2.setString(1, certificateNo);
                        pst2.setString(2, flightID.substring(11, flightID.length()));
                        pst2.setString(3, flightID.substring(0,10));
                        ret2 = pst2.executeQuery();
                        if (ret2.next()) {
                            tmp.setPassengerName(ret2.getString(1));
                            tmp.setMobile(ret2.getString(2));
                            tmp.setCertificateNo(ret2.getString(3));
                            tmp.setFlightNo(flightID.substring(11, flightID.length()));
                            tmp.setFlightDate(flightID.substring(0, 10));
                            tmp.setCabinClass(ret2.getString(4));
                            tmp.setDptAirport(ret2.getString(5));
                            tmp.setDptAptCode(ret2.getString(6));
                            tmp.setArvAirport(ret2.getString(7));
                            tmp.setArvAptCode(ret2.getString(8));
                            tmp.setDepTime(ret2.getString(9).substring(0,19));
                            tmp.setArrTime(ret2.getString(10).substring(0,19));
                            tmp.setAuctionID(tmpData.getString("auction"));
                            tmp.setAuctionState(tmpData.getString("status"));
                            tmp.setAuctionType(tmpData.getString("type"));
                            tmp.setDescription(tmpData.getString("description"));
                            history.add(tmp);
                        }
                        ret2.close();
                        pst2.close();
                    }
                    res.setAuth(1);
                    res.setCode(0);
                    res.setHistory(history);
                    history.clear();
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
