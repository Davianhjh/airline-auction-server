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
            String verifySql = "SELECT id FROM customerToken INNER JOIN customerAccount ON customerToken.uid = customerAccount.id WHERE token = ? and expire > ?;";
            pst = conn.prepareStatement(verifySql);
            pst.setString(1, AgiToken);
            pst.setString(2, utcTimeStr);
            ret = pst.executeQuery();
            if (ret.next()) {
                int uid = ret.getInt(1);
                ArrayList<baseTicketData> tickets = new ArrayList<baseTicketData>();
                if (getTicketsUtil.getLocalAddedTickets(conn, uid, utcTimeStr, tickets)) {
                    res.setAuth(1);
                    res.setCode(0);
                    res.setTickets(tickets);
                    return res;
                } else {
                    res.setAuth(-2);
                    res.setCode(1060);                          // get auctionData error
                    return res;
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
