package com.airline.auction;

import com.airline.baseTicketData;
import com.airline.tools.*;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.sql.*;
import java.util.ArrayList;

@Path("/addTicket")
public class addTicket {
    private static final int TAMPORARY_UID = 888888888;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public addTicketRes addTicket (@Context HttpHeaders hh, addTicketParam at) {
        MultivaluedMap<String, String> header = hh.getRequestHeaders();
        String AgiToken = header.getFirst("token");
        addTicketRes res = new addTicketRes();
        ArrayList<baseTicketData> tickets = new ArrayList<baseTicketData>();
        Connection conn;
        PreparedStatement pst;
        ResultSet ret, ret2, ret3;
        int result;
        boolean verifyResult = verifyAddTicketParams(at);
        if (!verifyResult) {
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
            // for member add ticket
            if (AgiToken != null) {
                String utcTimeStr = UTCTimeUtil.getUTCTimeStr();
                String verifySql = "SELECT id FROM customerToken INNER JOIN customerAccount ON customerToken.uid = customerAccount.id WHERE token = ? and expire > ?;";
                pst = conn.prepareStatement(verifySql);
                pst.setString(1, AgiToken);
                pst.setString(2, utcTimeStr);
                ret = pst.executeQuery();
                if (ret.next()) {
                    int uid = ret.getInt(1);
                    result = getTicketsUtil.getRemoteTickets(conn, uid, at.getName(), at.getNumber(), at.getNumberType(), tickets, null);
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
                    res.setAuth(-1);
                    res.setCode(1020);                              // user not found
                    return res;
                }
            }
            // for visitor add ticket
            else {
                if (at.getNumberType() == 1) {
                    res.setAuth(-1);
                    res.setCode(1023);                                  // visitor cannot use certificateNo search tickets
                    return res;
                } else {
                    result = getTicketsUtil.getRemoteTickets(conn, TAMPORARY_UID, at.getName(), at.getNumber(), at.getNumberType(), tickets, null);
                    if (result == 1) {
                        if (tickets.size() == 0) {
                            res.setAuth(1);
                            res.setCode(0);
                            res.setTickets(tickets);             // ticketNo not found, tickets is [];
                            return res;
                        } else {
                            String token = null;
                            int uid;
                            String sql1 = "SELECT id, username FROM customerAccount WHERE tel=?;";
                            pst= conn.prepareStatement(sql1);
                            pst.setString(1, at.getTel());
                            ret2 = pst.executeQuery();
                            if (ret2.next()) {
                                uid = ret2.getInt(1);
                                String userName = ret2.getString(2);
                                token = tokenHandler.createJWT(String.valueOf(uid), userName, "mobile", 7 * 24 * 3600 * 1000);
                                String sql2 = "UPDATE customerToken set token=?, expire=ADDTIME(utc_timestamp(), '7 00:00:00') WHERE uid=?;";
                                pst = conn.prepareStatement(sql2);
                                pst.setString(1, token);
                                pst.setInt(2, uid);
                                pst.executeUpdate();
                            } else {
                                String userName = MD5Util.getMD5(at.getTel());
                                String sql3 = "INSERT INTO customerAccount (tel_country, tel, username, platform) VALUES ('86',?,?,'mobile');";
                                pst = conn.prepareStatement(sql3, Statement.RETURN_GENERATED_KEYS);
                                pst.setString(1, at.getTel());
                                pst.setString(2, userName);
                                pst.executeUpdate();
                                ret3 = pst.getGeneratedKeys();
                                if (ret3.next()) {
                                    uid = ret3.getInt(1);
                                    token = tokenHandler.createJWT(String.valueOf(uid), userName, "mobile", 7 * 24 * 3600 * 1000);
                                    String sql4 = "INSERT INTO customerToken (uid, token, platform, expire) VALUES (?,?,'mobile', ADDTIME(utc_timestamp(), '7 00:00:00'));";
                                    pst = conn.prepareStatement(sql4);
                                    pst.setInt(1, uid);
                                    pst.setString(2, token);
                                    pst.executeUpdate();
                                } else {
                                    res.setAuth(-2);
                                    res.setCode(2000);         // mysql error (shouldn't be here)
                                    return res;
                                }
                            }
                            String deleteSql = "DELETE FROM passengerFlight WHERE addedByUid=? AND ticketNo=?";
                            pst = conn.prepareStatement(deleteSql);
                            pst.setInt(1, uid);
                            pst.setString(2, at.getNumber());
                            pst.executeUpdate();

                            String updateSql = "UPDATE passengerFlight set addedByUid=?, timeStamp=? WHERE addedByUid=? AND ticketNo=?;";
                            pst = conn.prepareStatement(updateSql);
                            pst.setInt(1, uid);
                            pst.setString(2, UTCTimeUtil.getUTCTimeStr());
                            pst.setInt(3, TAMPORARY_UID);
                            pst.setString(4, at.getNumber());
                            pst.executeUpdate();
                            res.setAuth(1);
                            res.setCode(0);
                            res.setName(at.getName());
                            res.setToken(token);
                            res.setTickets(tickets);
                            return res;
                        }
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
        } catch (SQLException e) {
            e.printStackTrace();
            res.setAuth(-2);
            res.setCode(2000);                           // mysql error
            return res;
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean verifyAddTicketParams(addTicketParam at) {
        try {
            return  ((at.getNumberType() == 1 || at.getNumberType() == 2) && at.getName() != null && at.getTel() != null && at.getNumber() != null);
        } catch (RuntimeException e) {
            return false;
        }
    }
}
