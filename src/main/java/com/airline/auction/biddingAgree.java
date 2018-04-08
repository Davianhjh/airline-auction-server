package com.airline.auction;

import com.airline.tools.HiKariCPHandler;
import com.airline.tools.UTCTimeUtil;
import com.airline.tools.auctionInfo;
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

@Path("/biddingAgree")
public class biddingAgree {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public biddingAgreeRes agree (@Context HttpHeaders hh, biddingAgreeParam ba) {
        MultivaluedMap<String, String> header = hh.getRequestHeaders();
        String AgiToken = header.getFirst("token");
        biddingAgreeRes res = new biddingAgreeRes();
        Connection conn;
        PreparedStatement pst;
        ResultSet ret;
        boolean verifyResult = verifyBiddingAgreeParam(ba);
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
                auctionInfo ai = getAuctionUtil.getAuctionStatus(ba.getAuctionID());
                if (ai == null || ai.getAuctionState() == null) {
                    res.setAuth(-2);
                    res.setCode(1060);                       // auction server error
                    return res;
                } else if (ai.getAuctionState().equals("active")) {
                    String searchSql = "SELECT userStatus FROM userState WHERE auctionID=? AND uid=? AND certificateNo=?;";
                    pst = conn.prepareStatement(searchSql);
                    pst.setString(1, ba.getAuctionID());
                    pst.setInt(2, uid);
                    pst.setString(3, ba.getCertificateNo());
                    ret = pst.executeQuery();
                    if (ret.next()) {
                        res.setAuth(1);
                        res.setCode(0);
                        res.setAgree(1);
                        return res;
                    } else {
                        String insertSql = "INSERT INTO userState (auctionID, uid, certificateNo, userStatus, timeStamp) VALUES (?,?,?,?,?);";
                        pst = conn.prepareStatement(insertSql);
                        pst.setString(1, ba.getAuctionID());
                        pst.setInt(2, uid);
                        pst.setString(3, ba.getCertificateNo());
                        pst.setInt(4, 1);
                        pst.setString(5, utcTimeStr);
                        pst.executeUpdate();
                        res.setAuth(1);
                        res.setCode(0);
                        res.setAgree(1);
                        return res;
                    }
                } else {
                    res.setAuth(-1);
                    res.setCode(1030);                       // error auctionState
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

    private boolean verifyBiddingAgreeParam (biddingAgreeParam ba) {
        try {
            return (ba.getAuctionID() != null) && (ba.getCertificateNo() != null);
        } catch (RuntimeException e) {
            return false;
        }
    }
}
