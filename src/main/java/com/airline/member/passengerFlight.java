package com.airline.member;

import com.airline.baseTicketData;
import com.airline.tools.HiKariCPHandler;
import com.airline.tools.UTCTimeUtil;
import com.airline.tools.getTicketsUtil;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
                     if (getTicketsUtil.getLocalAddedTickets(conn, uid, utcTimeStr, tickets)) {
                        res.setAuth(1);
                        res.setCode(1);                          // a remind for bounding certificateCard
                        res.setTickets(tickets);
                        return res;
                    } else {
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
                            res.setAuth(-2);
                            res.setCode(2000);                                  // date parse error
                            return res;
                        }
                        if (timeInterval > MAX_INTERVAL) {
                            int result = getTicketsUtil.getRemoteTickets(conn, uid, cnid_name, cnid, 1, tickets, savedEdgeTime);
                            if (result == 1) {
                                res.setAuth(1);
                                res.setCode(0);
                                res.setTickets(tickets);
                                return res;
                            } else if (result == -1) {
                                res.setAuth(-2);
                                res.setCode(1050);                       // get ticketData error
                                return res;
                            } else {
                                res.setAuth(-2);
                                res.setCode(1060);                       // get auctionData error
                                return res;
                            }
                        } else {
                            if (getTicketsUtil.getLocalAddedTickets(conn, uid, utcTimeStr, tickets)) {
                                res.setAuth(1);
                                res.setCode(0);
                                res.setTickets(tickets);
                                return res;
                            } else {
                                res.setAuth(-2);
                                res.setCode(1060);                       // get auctionData error
                                return res;
                            }
                        }
                    } else {
                        int result = getTicketsUtil.getRemoteTickets(conn, uid, cnid_name, cnid, 1, tickets, null);
                        if (result == 1) {
                            res.setAuth(1);
                            res.setCode(0);
                            res.setTickets(tickets);
                            return res;
                        } else if (result == -1) {
                            res.setAuth(-2);
                            res.setCode(1050);                       // get ticketData error
                            return res;
                        } else {
                            res.setAuth(-2);
                            res.setCode(1060);                       // get auctionData error
                            return res;
                        }
                    }
                }
            } else {
                res.setAuth(-1);
                res.setCode(1020);                              // user not found
                return res;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            res.setAuth(-2);
            res.setCode(2000);                                  // mysql error
            return res;
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
