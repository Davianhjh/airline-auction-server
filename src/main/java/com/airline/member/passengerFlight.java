package com.airline.member;

import com.airline.baseAuctionData;
import com.airline.baseTicketData;
import com.airline.tools.HiKariCPHandler;
import com.airline.tools.UTCTimeUtil;

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
import java.util.ArrayList;

@Path("/member/passengerFlight")
public class passengerFlight {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public passengerFlightRes flightList (@Context HttpHeaders hh){
        MultivaluedMap<String, String> header = hh.getRequestHeaders();
        String AgiToken = header.getFirst("token");
        passengerFlightRes res = new passengerFlightRes();
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
            res.setCode(2000);                                   // fail to get mysql connection
            return res;
        }
        try {
            String utcTimeStr = UTCTimeUtil.getUTCTimeStr();
            String verifySql = "SELECT id, cnid, cnid_name FROM customerToken INNER JOIN customerAccount ON customerToken.uid = customerAccount.id WHERE token = ? and expire > ?;";
            pst = conn.prepareStatement(verifySql);
            pst.setString(1, AgiToken);
            pst.setString(2, utcTimeStr);
            ret = pst.executeQuery();
            if(ret.next()){
                int uid = ret.getInt(1);
                String cnid = ret.getString(2);
                String cnid_name = ret.getString(3);
                ArrayList<baseTicketData> tickets = new ArrayList<baseTicketData>();
                if(cnid == null || cnid_name == null){
                    getAddedTickets(conn, uid, utcTimeStr, tickets);
                }
                return null;
            }
            else {
                conn.close();
                res.setAuth(-1);
                res.setCode(1020);                              // user not found
                return res;
            }
        } catch (SQLException e){
            e.printStackTrace();
            res.setAuth(-2);
            res.setCode(2000);                                  // mysql error
            return res;
        }
    }

    private void getAddedTickets(Connection conn, int uid, String utcTimeStr, ArrayList<baseTicketData> tickets) throws SQLException {
        PreparedStatement pst;
        ResultSet ret;
        ArrayList<String> flightArray = new ArrayList<String>();
        String sql1 = "SELECT passengerName, certificateNo, flightNo, flightDate, ticketNo, carbinClass, dptAirport, dptAptCode, arvAirport, arvAptCode, depTime, arrTime FROM passengerFlight WHERE addedByUid=? AND depTime > ?;";
        pst = conn.prepareStatement(sql1);
        pst.setInt(1, uid);
        pst.setString(2, UTCTimeUtil.getLocalTimeFromUTC(utcTimeStr));
        ret = pst.executeQuery();
        while(ret.next()){
            baseTicketData ticketData = new baseTicketData();
            ArrayList<baseAuctionData> auctionData = new ArrayList<baseAuctionData>();
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
            ticketData.setAuctions(auctionData);
            tickets.add(ticketData);
            flightArray.add(ret.getString(4) + "-" + ret.getString(3));
        }
        if(flightArray.size() != 0){

        }
    }
}
