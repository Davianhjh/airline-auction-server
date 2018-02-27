package com.airline.member;

import com.airline.baseAuctionData;
import com.airline.baseTicketData;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Properties;
import java.util.TimeZone;

@Path("/member/passengerFlight")
public class passengerFlight {
    private static final int MAX_INTERVAL = 600000;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public passengerFlightRes flightList (@Context HttpHeaders hh){
        MultivaluedMap<String, String> header = hh.getRequestHeaders();
        String AgiToken = header.getFirst("token");
        passengerFlightRes res = new passengerFlightRes();
        Connection conn;
        PreparedStatement pst, pst2;
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
            String verifySql = "SELECT id, cnid, cnid_name FROM customerToken INNER JOIN customerAccount ON customerToken.uid = customerAccount.id WHERE token = ? and expire > ?;";
            pst = conn.prepareStatement(verifySql);
            pst.setString(1, AgiToken);
            pst.setString(2, utcTimeStr);
            ret = pst.executeQuery();
            if (ret.next()) {
                int uid = ret.getInt(1);
                String cnid = ret.getString(2);
                String cnid_name = ret.getString(3);
                ArrayList<baseTicketData> tickets = new ArrayList<baseTicketData>();
                if (cnid == null || cnid_name == null) {
                    if (getLocalAddedTickets(conn, uid, utcTimeStr, tickets)) {
                        conn.close();
                        res.setAuth(1);
                        res.setCode(0);
                        res.setTickets(tickets);
                        return res;
                    } else {
                        conn.close();
                        res.setAuth(-2);
                        res.setCode(1060);                       // get auctionData error
                        return res;
                    }
                } else {
                    String sql = "SELECT DATE_FORMAT(MAX(timeStamp), '%Y-%m-%d %T') as ts FROM passengerFlight WHERE passengerName=? AND certificateNo=? AND certificateType='IN' AND addedByUid=?;";
                    pst2 = conn.prepareStatement(sql);
                    pst2.setString(1, cnid_name);
                    pst2.setString(2, cnid);
                    pst2.setInt(3, uid);
                    ret2 = pst2.executeQuery();
                    if (ret2.next() && ret2.getString(1) != null) {
                        long currentTimeStamp = System.currentTimeMillis();
                        long savedTime;
                        long timeInterval;
                        String savedEdgeTime = ret2.getString(1);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        try {
                            sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
                            savedTime = sdf.parse(savedEdgeTime).getTime();
                            timeInterval = currentTimeStamp - savedTime;
                        } catch (ParseException e) {
                            e.printStackTrace();
                            conn.close();
                            res.setAuth(-2);
                            res.setCode(2000);                                  // date parse error
                            return res;
                        }
                        if (timeInterval > MAX_INTERVAL) {
                            int result = getRemoteTickets(conn, uid, cnid_name, cnid, tickets, savedEdgeTime);
                            if (result == 1) {
                                conn.close();
                                res.setAuth(1);
                                res.setCode(0);
                                res.setTickets(tickets);
                                return res;
                            } else if (result == -1) {
                                conn.close();
                                res.setAuth(-2);
                                res.setCode(1050);                       // get ticketData error
                                return res;
                            } else {
                                conn.close();
                                res.setAuth(-2);
                                res.setCode(1060);                       // get auctionData error
                                return res;
                            }
                        } else {
                            if (getLocalAddedTickets(conn, uid, utcTimeStr, tickets)) {
                                conn.close();
                                res.setAuth(1);
                                res.setCode(0);
                                res.setTickets(tickets);
                                return res;
                            } else {
                                conn.close();
                                res.setAuth(-2);
                                res.setCode(1060);                       // get auctionData error
                                return res;
                            }
                        }
                    } else {
                        int result = getRemoteTickets(conn, uid, cnid_name, cnid, tickets, null);
                        if (result == 1) {
                            conn.close();
                            res.setAuth(1);
                            res.setCode(0);
                            res.setTickets(tickets);
                            return res;
                        } else if (result == -1) {
                            conn.close();
                            res.setAuth(-2);
                            res.setCode(1050);                       // get ticketData error
                            return res;
                        } else {
                            conn.close();
                            res.setAuth(-2);
                            res.setCode(1060);                       // get auctionData error
                            return res;
                        }
                    }
                }
            } else {
                conn.close();
                res.setAuth(-1);
                res.setCode(1020);                              // user not found
                return res;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            res.setAuth(-2);
            res.setCode(2000);                                  // mysql error
            return res;
        }
    }

    private boolean getLocalAddedTickets(Connection conn, int uid, String utcTimeStr, ArrayList<baseTicketData> tickets) throws SQLException {
        PreparedStatement pst;
        ResultSet ret;
        ArrayList<String> flightArray = new ArrayList<String>();
        ArrayList<baseAuctionData> auctions = new ArrayList<baseAuctionData>();
        baseAuctionData auctionData = new baseAuctionData();
        String resStr;
        Properties serverProp = new Properties();
        String sql = "SELECT passengerName, certificateNo, flightNo, flightDate, ticketNo, carbinClass, dptAirport, dptAptCode, arvAirport, arvAptCode, depTime, arrTime FROM passengerFlight WHERE addedByUid=? AND depTime > ?;";
        pst = conn.prepareStatement(sql);
        pst.setInt(1, uid);
        pst.setString(2, UTCTimeUtil.getLocalTimeFromUTC(utcTimeStr));
        ret = pst.executeQuery();
        while(ret.next()){
            baseTicketData ticketData = new baseTicketData();
            ticketData.setPassengerName(ret.getString(1));
            ticketData.setCertificateNo(ret.getString(2));
            ticketData.setFlightNo(ret.getString(3));
            ticketData.setFlightDate(ret.getString(4));
            ticketData.setTicketNo(ret.getString(5));
            ticketData.setCabinClass(ret.getString(6));
            ticketData.setDptAirport(ret.getString(7));
            ticketData.setDptAptCode(ret.getString(8));
            ticketData.setArvAirport(ret.getString(9));
            ticketData.setArvAptCode(ret.getString(10));
            ticketData.setDepTime(ret.getString(11));
            ticketData.setArrTime(ret.getString(12));
            ticketData.setAuctions(auctions);
            tickets.add(ticketData);
            flightArray.add(ret.getString(4).substring(0,10) + "-" + ret.getString(3));
        }
        if(flightArray.size() != 0){
            try {
                InputStream in = getClass().getResourceAsStream("/serverAddress.properties");
                serverProp.load(in);
                String urlStr = serverProp.getProperty("auctionServiceServer") + "/auction/flights";
                JSONObject body = new JSONObject();
                body.put("flights", flightArray);
                resStr = httpRequestUtil.postRequest(urlStr, null, body.toJSONString());
            } catch (Exception e){
                e.printStackTrace();
                return false;                                 // request failed OR bad response
            }
            JSONObject response = JSONObject.parseObject(resStr);
            for(int i=0; i<flightArray.size(); i++){
                JSONArray flightAuction = response.getJSONArray(flightArray.get(i));
                if(flightAuction.size() != 0){
                    for(int j=0; j<flightAuction.size(); j++){
                        JSONObject auction = flightAuction.getJSONObject(j);
                        auctionData.setAuctionID(auction.getString("id"));
                        auctionData.setAuctionType(auction.getString("type"));
                        auctionData.setAuctionState(auction.getString("status"));
                        auctionData.setStartTime(auction.getBigInteger("start"));
                        auctionData.setEndTime(auction.getBigInteger("end"));
                        auctionData.setStartCountDown(auction.getIntValue("startCountDown"));
                        auctionData.setEndCountDown(auction.getIntValue("endCountDown"));
                        auctionData.setDescription(auction.getString("description"));
                        auctions.add(auctionData);
                    }
                    tickets.get(i).setAuctions(auctions);
                    auctions.clear();
                }
            }
            flightArray.clear();
        }
        return true;
    }

    private int getRemoteTickets(Connection conn, int uid, String cnid_name, String cnid, ArrayList<baseTicketData> tickets, String savedEdgeTime) throws SQLException {
        PreparedStatement pst, pst2;
        String sql = "INSERT INTO passengerFlight (passengerName, flightNo, flightDate, ticketNo, certificateType, certificateNo, carbinClass, dptAirport, dptAptCode, arvAirport, arvAptCode, depTime, arrTime, addedByUid, timeStamp) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        pst = conn.prepareStatement(sql);
        ArrayList<String> flightArray = new ArrayList<String>();
        ArrayList<baseAuctionData> auctions = new ArrayList<baseAuctionData>();
        baseAuctionData auctionData = new baseAuctionData();
        String resStr1, resStr2;
        Properties serverProp = new Properties();

        if(savedEdgeTime != null){
            String deleteSql = "DELETE FROM passengerFlight where timeStamp=?;";
            pst2 = conn.prepareStatement(deleteSql);
            pst2.setString(1,savedEdgeTime);
            pst2.executeUpdate();
        }

        try {
            InputStream in = getClass().getResourceAsStream("/serverAddress.properties");
            serverProp.load(in);
            String urlStr1 = serverProp.getProperty("airlineMiddlewareServer") + "/rest/ticketList";
            JSONObject body1 = new JSONObject();
            body1.put("passengerName", cnid_name);
            body1.put("passengerID", cnid);
            body1.put("idType", "IN");
            resStr1 = httpRequestUtil.postRequest(urlStr1, null, body1.toJSONString());
        } catch (Exception e){
            e.printStackTrace();
            return -1;                        // request failed OR bad response
        }
        JSONObject response1 = JSONObject.parseObject(resStr1);
        JSONArray ticketList = response1.getJSONArray("tickets");
        if(ticketList.size() != 0){
            for(int i=0; i<ticketList.size(); i++){
                JSONObject flightTicket = ticketList.getJSONObject(i);
                baseTicketData ticketData = new baseTicketData();
                ticketData.setPassengerName(flightTicket.getString("passengerName"));
                ticketData.setCertificateNo(flightTicket.getString("certificateNo"));
                ticketData.setFlightNo(flightTicket.getString("flightNo"));
                ticketData.setFlightDate(flightTicket.getString("flightDate"));
                ticketData.setTicketNo(flightTicket.getString("ticketNo"));
                ticketData.setCabinClass(flightTicket.getString("carbinClass"));
                ticketData.setDptAirport(flightTicket.getString("dptAirport"));
                ticketData.setDptAptCode(flightTicket.getString("dptAptCode"));
                ticketData.setArvAirport(flightTicket.getString("arvAirport"));
                ticketData.setArvAptCode(flightTicket.getString("arvAptCode"));
                ticketData.setDepTime(flightTicket.getString("depTime"));
                ticketData.setArrTime(flightTicket.getString("arrTime"));
                ticketData.setAuctions(auctions);
                tickets.add(ticketData);
                flightArray.add(flightTicket.getString("flightDate") + "-" + flightTicket.getString("flightNo"));

                pst.setString(1, flightTicket.getString("passengerName"));
                pst.setString(2, flightTicket.getString("flightNo"));
                pst.setString(3, flightTicket.getString("flightDate"));
                pst.setString(4, flightTicket.getString("ticketNo"));
                pst.setString(5, flightTicket.getString("certificateType"));
                pst.setString(6, flightTicket.getString("certificateNo"));
                pst.setString(7, flightTicket.getString("carbinClass"));
                pst.setString(8, flightTicket.getString("dptAirport"));
                pst.setString(9, flightTicket.getString("dptAptCode"));
                pst.setString(10, flightTicket.getString("arvAirport"));
                pst.setString(11, flightTicket.getString("arvAptCode"));
                pst.setString(12, flightTicket.getString("depTime"));
                pst.setString(13, flightTicket.getString("arrTime"));
                pst.setInt(14, uid);
                pst.setString(15, UTCTimeUtil.getUTCTimeStr());
                pst.addBatch();
            }
            pst.executeBatch();

            try {
                String urlStr2 = serverProp.getProperty("auctionServiceServer") + "/auction/flights";
                JSONObject body = new JSONObject();
                body.put("flights", flightArray);
                resStr2 = httpRequestUtil.postRequest(urlStr2, null, body.toJSONString());
            } catch (Exception e){
                e.printStackTrace();
                return -2;                    // request failed OR bad response
            }
            JSONObject response = JSONObject.parseObject(resStr2);
            for(int i=0; i<flightArray.size(); i++){
                JSONArray flightAuction = response.getJSONArray(flightArray.get(i));
                if(flightAuction.size() != 0){
                    for(int j=0; j<flightAuction.size(); j++){
                        JSONObject auction = flightAuction.getJSONObject(j);
                        auctionData.setAuctionID(auction.getString("id"));
                        auctionData.setAuctionType(auction.getString("type"));
                        auctionData.setAuctionState(auction.getString("status"));
                        auctionData.setStartTime(auction.getBigInteger("start"));
                        auctionData.setEndTime(auction.getBigInteger("end"));
                        auctionData.setStartCountDown(auction.getIntValue("startCountDown"));
                        auctionData.setEndCountDown(auction.getIntValue("endCountDown"));
                        auctionData.setDescription(auction.getString("description"));
                        auctions.add(auctionData);
                    }
                    tickets.get(i).setAuctions(auctions);
                    auctions.clear();
                }
            }
            flightArray.clear();
        }
        return 1;
    }
}
