package com.airline.auction;

import com.airline.tools.HiKariCPHandler;
import com.airline.tools.UTCTimeUtil;
import com.airline.tools.getAuctionUtil;

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

@Path("/passengerState")
public class passengerState {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public passengerResult getPassengerState (@Context HttpHeaders hh, passengerStateParam ps) {
        MultivaluedMap<String, String> header = hh.getRequestHeaders();
        String AgiToken = header.getFirst("token");
        passengerResult res = new passengerResult();
        Connection conn;
        PreparedStatement pst;
        ResultSet ret,ret2,ret3;
        boolean verifyResult = verifyPassengerStateParam(ps);
        if (AgiToken == null || !verifyResult) {
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
                String paymentVerifySql = "SELECT paymentState FROM tradeRecord WHERE uid=? AND auctionID=? AND certificateNo=?;";
                pst = conn.prepareStatement(paymentVerifySql);
                pst.setInt(1, uid);
                pst.setString(2, ps.getAuctionID());
                pst.setString(3, ps.getCertificateNo());
                ret2 = pst.executeQuery();
                res.setPaymentState(false);
                if (ret2.next() && ret2.getInt(1) == 1) {
                    res.setPaymentState(true);
                }

                String searchSql = "SELECT uid, userStatus FROM userState WHERE auctionID=? AND certificateNo=?;";
                pst = conn.prepareStatement(searchSql);
                pst.setString(1, ps.getAuctionID());
                pst.setString(2, ps.getCertificateNo());
                ret3 = pst.executeQuery();
                int biddingTag = 0;
                int userStatus = 0;
                while (ret3.next()) {
                    if (ret3.getInt(1) == uid) {
                        userStatus = ret3.getInt(2);
                    }
                    if (ret3.getInt(1) != uid && ret3.getInt(2) != 1)
                        biddingTag = 1;
                }
                try {
                    res = getAuctionUtil.getBiddingResult(uid, ps.getAuctionID(), ps.getCertificateNo());
                    if (res.getAuctionType() == null || res.getAuctionState() == null) {
                        res.setAuth(-1);                                // auction not found
                        res.setCode(1040);
                        return res;
                    }

                    if (biddingTag == 0) {                              // agreed and not bid by someone else
                        res.setAuth(1);
                        res.setCode(0);
                        res.setUserStatus(userStatus);
                        return res;
                    } else {                                            // bid by someone else
                        res.setAuth(1);
                        res.setCode(0);
                        res.setUserStatus(-1);
                        return res;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    res.setAuth(-2);
                    res.setCode(1060);                                  // auction service error
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

    private boolean verifyPassengerStateParam(passengerStateParam ps) {
        try {
            return (ps.getAuctionID() != null) && (ps.getCertificateNo() != null);
        } catch (RuntimeException e) {
            return false;
        }
    }
}
