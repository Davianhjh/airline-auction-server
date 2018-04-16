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
                    result = getTicketsUtil.getRemoteTickets(conn, uid, at.getTicketNo(), at.getPassengerName(), at.getTel(), tickets);
                    if (result == 1) {
                        res.setAuth(1);
                        res.setCode(0);
                        res.setTickets(tickets);
                        return res;
                    } else if (result == 0) {
                        res.setAuth(-1);
                        res.setCode(1040);                       // ticket not found OR not exist
                        return res;
                    } else if (result == -1) {
                        res.setAuth(-2);
                        res.setCode(1050);                       // middleware server error
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
            }
            // for visitor add ticket
            else {
                result = getTicketsUtil.getRemoteTickets(conn, TAMPORARY_UID, at.getTicketNo(), at.getPassengerName(), at.getTel(), tickets);
                if (result == 1) {
                    String token,userName;
                    int uid;
                    String sql1 = "SELECT id, username FROM customerAccount WHERE tel=? AND tel_country=?;";
                    pst= conn.prepareStatement(sql1);
                    pst.setString(1, at.getTel());
                    pst.setString(2, at.getTelCountry());
                    ret2 = pst.executeQuery();
                    if (ret2.next()) {
                        uid = ret2.getInt(1);
                        userName = ret2.getString(2);
                        token = tokenHandler.createJWT(String.valueOf(uid), userName, "mobile", 7 * 24 * 3600 * 1000);
                        String sql2 = "SELECT tid FROM customerToken WHERE uid=?";
                        pst = conn.prepareStatement(sql2);
                        pst.setInt(1, uid);
                        ret3 = pst.executeQuery();
                        if (ret3.next()) {
                            String sql3 = "UPDATE customerToken set token=?, expire=ADDTIME(utc_timestamp(), '7 00:00:00') WHERE uid=?;";
                            pst = conn.prepareStatement(sql3);
                            pst.setString(1, token);
                            pst.setInt(2, uid);
                            pst.executeUpdate();
                        } else {
                            String sql4 = "INSERT INTO customerToken (uid, token, platform, expire) VALUES (?,?,'mobile', ADDTIME(utc_timestamp(), '7 00:00:00'));";
                            pst = conn.prepareStatement(sql4);
                            pst.setInt(1, uid);
                            pst.setString(2, token);
                            pst.executeUpdate();
                        }
                        res.setNewComer(0);
                        res.setName(userName);
                    } else {
                        userName = MD5Util.getMD5(at.getTel());
                        if (userName == null) {
                            res.setAuth(-2);
                            res.setCode(2000);                    // MD5 error
                            return res;
                        }
                        res.setName(userName.substring(0,10));
                        String sql3 = "INSERT INTO customerAccount (tel_country, tel, username, platform) VALUES (?,?,?,'mobile');";
                        pst = conn.prepareStatement(sql3, Statement.RETURN_GENERATED_KEYS);
                        pst.setString(1, at.getTelCountry());
                        pst.setString(2, at.getTel());
                        pst.setString(3, userName.substring(0, 10));
                        pst.executeUpdate();
                        ResultSet rst = pst.getGeneratedKeys();
                        if (rst.next()) {
                            uid = rst.getInt(1);
                            token = tokenHandler.createJWT(String.valueOf(uid), userName, "mobile", 7 * 24 * 3600 * 1000);
                            String sql4 = "INSERT INTO customerToken (uid, token, platform, expire) VALUES (?,?,'mobile', ADDTIME(utc_timestamp(), '7 00:00:00'));";
                            pst = conn.prepareStatement(sql4);
                            pst.setInt(1, uid);
                            pst.setString(2, token);
                            pst.executeUpdate();
                            res.setNewComer(1);
                        } else {
                            res.setAuth(-2);
                            res.setCode(2000);         // mysql error (shouldn't be here)
                            return res;
                        }
                    }
                    String deleteSql = "DELETE FROM passengerFlight WHERE addedByUid=? AND ticketNo=?";
                    pst = conn.prepareStatement(deleteSql);
                    pst.setInt(1, uid);
                    pst.setString(2, at.getTicketNo());
                    pst.executeUpdate();

                    String updateSql = "UPDATE passengerFlight set addedByUid=?, timeStamp=? WHERE addedByUid=? AND ticketNo=?;";
                    pst = conn.prepareStatement(updateSql);
                    pst.setInt(1, uid);
                    pst.setString(2, UTCTimeUtil.getUTCTimeStr());
                    pst.setInt(3, TAMPORARY_UID);
                    pst.setString(4, at.getTicketNo());
                    pst.executeUpdate();
                    res.setAuth(1);
                    res.setCode(0);
                    res.setToken(token);
                    res.setTickets(tickets);
                    return res;
                } else if (result == 0) {
                    res.setAuth(-1);
                    res.setCode(1040);                       // ticket not found OR not exist
                    return res;
                } else if (result == -1) {
                    res.setAuth(-2);
                    res.setCode(1050);                       // middleware server error
                    return res;
                } else {
                    res.setAuth(-2);
                    res.setCode(1060);                       // auction server error
                    return res;
                }
            }
        } catch (SQLException e) {
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

    private boolean verifyAddTicketParams(addTicketParam at) {
        try {
            return  (at.getTicketNo() != null && at.getPassengerName() != null && at.getTel() != null && at.getTelCountry() != null);
        } catch (RuntimeException e) {
            return false;
        }
    }
}
