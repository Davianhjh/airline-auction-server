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
import java.text.NumberFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Path("/biddingPrice")
public class biddingPrice {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public biddingPriceRes bid (@Context HttpHeaders hh, biddingPriceParam bp) {
        MultivaluedMap<String, String> header = hh.getRequestHeaders();
        String AgiToken = header.getFirst("token");
        biddingPriceRes res = new biddingPriceRes();
        Connection conn;
        PreparedStatement pst;
        ResultSet ret;
        boolean verifyResult = verifyBiddingPriceParam(bp);
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
                String searchSql = "SELECT userStatus FROM userState WHERE auctionID=? AND uid=? AND certificateNo=?;";
                pst = conn.prepareStatement(searchSql);
                pst.setString(1, bp.getAuctionID());
                pst.setInt(2, uid);
                pst.setString(3, bp.getCertificateNo());
                ret = pst.executeQuery();
                if (ret.next()) {
                    int userStatus = ret.getInt(1);
                    if (userStatus == 1) {
                        if (getAuctionUtil.bidForPrice(uid, bp.getAuctionID(), bp.getCertificateNo(), bp.getPrice())) {
                            String updateSql = "UPDATE userState set userStatus=2, timeStamp=? WHERE auctionID=? AND uid=? AND certificateNo=?;";
                            pst = conn.prepareStatement(updateSql);
                            pst.setString(1, utcTimeStr);
                            pst.setString(2, bp.getAuctionID());
                            pst.setInt(3, uid);
                            pst.setString(4, bp.getCertificateNo());
                            pst.executeUpdate();
                            res.setAuth(1);
                            res.setCode(0);
                            res.setBid(1);
                            return res;
                        } else {
                            res.setAuth(-2);
                            res.setCode(1060);                  // auction service error OR auction not found
                            return res;
                        }
                    } else {
                        res.setAuth(-1);
                        res.setCode(1031);                      // error userStatus
                        return res;
                    }
                } else {
                    res.setAuth(-1);
                    res.setCode(1031);                          // error userStatus
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

    private boolean verifyBiddingPriceParam (biddingPriceParam bp) {
        try {
            Pattern pattern= Pattern.compile("^(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){0,2})?$");
            if((bp.getAuctionID() == null) || (bp.getCertificateNo() == null) || (bp.getPrice() <= 0))
                return false;
            else  {
                NumberFormat nf = NumberFormat.getInstance();
                nf.setGroupingUsed(false);
                String priceStr = nf.format(bp.getPrice());
                Matcher match = pattern.matcher(priceStr);
                return match.matches();
            }
        } catch (RuntimeException e) {
            return false;
        }
    }
}
