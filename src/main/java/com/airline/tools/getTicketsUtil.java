package com.airline.tools;

import com.airline.baseAuctionData;
import com.airline.baseTicketData;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

public class getTicketsUtil {

    public static boolean getLocalAddedTickets(Connection conn, int uid, String utcTimeStr, ArrayList<baseTicketData> tickets) throws SQLException {
        PreparedStatement pst;
        ResultSet ret;
        ArrayList<String> flightArray = new ArrayList<String>();
        ArrayList<baseAuctionData> auctions = new ArrayList<baseAuctionData>();
        JSONObject response;
        Properties serverProp = new Properties();
        String sql = "SELECT passengerName, mobile, certificateNo, flightNo, flightDate, ticketNo, carbinClass, dptAirport, dptAptCode, arvAirport, arvAptCode, depTime, arrTime FROM passengerFlight WHERE addedByUid=? AND depTime > ?;";
        pst = conn.prepareStatement(sql);
        pst.setInt(1, uid);
        pst.setString(2, UTCTimeUtil.getLocalTimeFromUTC(utcTimeStr));
        ret = pst.executeQuery();
        while(ret.next()){
            baseTicketData ticketData = new baseTicketData();
            ticketData.setPassengerName(ret.getString(1));
            ticketData.setMobile(ret.getString(2));
            ticketData.setCertificateNo(ret.getString(3));
            ticketData.setFlightNo(ret.getString(4));
            ticketData.setFlightDate(ret.getString(5).substring(0,10));
            ticketData.setTicketNo(ret.getString(6));
            ticketData.setCabinClass(ret.getString(7));
            ticketData.setDptAirport(ret.getString(8));
            ticketData.setDptAptCode(ret.getString(9));
            ticketData.setArvAirport(ret.getString(10));
            ticketData.setArvAptCode(ret.getString(11));
            ticketData.setDepTime(ret.getString(12).substring(0,19));
            ticketData.setArrTime(ret.getString(13).substring(0,19));
            ticketData.setAuctions(auctions);
            tickets.add(ticketData);
            flightArray.add(ret.getString(5).substring(0,10) + "-" + ret.getString(4));
        }
        if(flightArray.size() != 0){
            try {
                InputStream in = getTicketsUtil.class.getResourceAsStream("/serverAddress.properties");
                serverProp.load(in);
                in.close();
                String urlStr = serverProp.getProperty("auctionServiceServer") + "/auction/flights";
                JSONObject body = new JSONObject();
                body.put("flights", flightArray);
                response = httpRequestUtil.postRequest(urlStr, null, body);
            } catch (Exception e){
                e.printStackTrace();
                return false;                                 // request failed OR bad response
            }
            for(int i=0; i<flightArray.size(); i++){
                JSONArray flightAuction = response.getJSONArray(flightArray.get(i));
                if(flightAuction.size() != 0){
                    for(int j=0; j<flightAuction.size(); j++){
                        JSONObject auction = flightAuction.getJSONObject(j);
                        baseAuctionData auctionData = new baseAuctionData();
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

    public static int getRemoteTickets(Connection conn, int uid, String ticketNo, String mobile, ArrayList<baseTicketData> tickets) throws SQLException {
        PreparedStatement pst;
        String sql = "INSERT IGNORE INTO passengerFlight (passengerName, mobile, flightNo, flightDate, ticketNo, certificateType, certificateNo, carbinClass, dptAirport, dptAptCode, arvAirport, arvAptCode, depTime, arrTime, addedByUid, timeStamp) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        pst = conn.prepareStatement(sql);
        ArrayList<String> flightArray = new ArrayList<String>();
        ArrayList<baseAuctionData> auctions = new ArrayList<baseAuctionData>();
        JSONObject response1, response2;
        Properties serverProp = new Properties();

        try {
            InputStream in = getTicketsUtil.class.getResourceAsStream("/serverAddress.properties");
            serverProp.load(in);
            in.close();
            JSONObject body1 = new JSONObject();
            String urlStr1;
            urlStr1 = serverProp.getProperty("airlineMiddlewareServer") + "/rest/ticketNoSearch";
            body1.put("ticketNo", ticketNo);
            response1 = httpRequestUtil.postRequest(urlStr1, null, body1);
        } catch (Exception e){
            e.printStackTrace();
            return -1;                                            // request failed OR bad response
        }
        JSONArray ticketList = response1.getJSONArray("tickets");
        if (ticketList.size() != 0) {
            for(int i=0; i<ticketList.size(); i++){
                JSONObject flightTicket = ticketList.getJSONObject(i);
                baseTicketData ticketData = new baseTicketData();
                ticketData.setPassengerName(flightTicket.getString("passengerName"));
                ticketData.setMobile(mobile);
                ticketData.setCertificateNo(flightTicket.getString("certificateNo"));
                ticketData.setFlightNo(flightTicket.getString("flightNo"));
                ticketData.setFlightDate(flightTicket.getString("flightDate").substring(0,10));
                ticketData.setTicketNo(flightTicket.getString("ticketNo"));
                ticketData.setCabinClass(flightTicket.getString("carbinClass"));
                ticketData.setDptAirport(flightTicket.getString("dptAirport"));
                ticketData.setDptAptCode(flightTicket.getString("dptAptCode"));
                ticketData.setArvAirport(flightTicket.getString("arvAirport"));
                ticketData.setArvAptCode(flightTicket.getString("arvAptCode"));
                ticketData.setDepTime(flightTicket.getString("depTime").substring(0,19));
                ticketData.setArrTime(flightTicket.getString("arrTime").substring(0,19));
                ticketData.setAuctions(auctions);
                tickets.add(ticketData);
                flightArray.add(flightTicket.getString("flightDate") + "-" + flightTicket.getString("flightNo"));

                pst.setString(1, flightTicket.getString("passengerName"));
                pst.setString(2, mobile);
                pst.setString(3, flightTicket.getString("flightNo"));
                pst.setString(4, flightTicket.getString("flightDate"));
                pst.setString(5, flightTicket.getString("ticketNo"));
                pst.setString(6, flightTicket.getString("certificateType"));
                pst.setString(7, flightTicket.getString("certificateNo"));
                pst.setString(8, flightTicket.getString("carbinClass"));
                pst.setString(9, flightTicket.getString("dptAirport"));
                pst.setString(10, flightTicket.getString("dptAptCode"));
                pst.setString(11, flightTicket.getString("arvAirport"));
                pst.setString(12, flightTicket.getString("arvAptCode"));
                pst.setString(13, flightTicket.getString("depTime"));
                pst.setString(14, flightTicket.getString("arrTime"));
                pst.setInt(15, uid);
                pst.setString(16, UTCTimeUtil.getUTCTimeStr());
                pst.addBatch();
            }
            pst.executeBatch();

            try {
                String urlStr2 = serverProp.getProperty("auctionServiceServer") + "/auction/flights";
                JSONObject body = new JSONObject();
                body.put("flights", flightArray);
                response2 = httpRequestUtil.postRequest(urlStr2, null, body);
            } catch (Exception e){
                e.printStackTrace();
                return -2;                                               // request failed OR bad response
            }
            for(int i=0; i<flightArray.size(); i++){
                JSONArray flightAuction = response2.getJSONArray(flightArray.get(i));
                if(flightAuction.size() != 0){
                    for(int j=0; j<flightAuction.size(); j++){
                        JSONObject auction = flightAuction.getJSONObject(j);
                        baseAuctionData auctionData = new baseAuctionData();
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
            return 1;
        }
        else return 0;                                     // ticket not found OR ticketNo error
    }
}
