package com.airline.lottery;

import com.airline.tools.*;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Properties;

@Path("/lottery/buyBalls")
public class buyBalls {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public createBillRes buyBalls (@Context HttpHeaders hh, buyBallsParam bb) {
        MultivaluedMap<String, String> header = hh.getRequestHeaders();
        String AgiToken = header.getFirst("token");
        createBillRes res = new createBillRes();
        Connection conn;
        PreparedStatement pst;
        ResultSet ret;
        boolean verifyResult = verifyBuyBallsParam(bb);
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
                pst.setString(1, bb.getAuctionID());
                pst.setInt(2, uid);
                pst.setString(3, bb.getCertificateNo());
                ret = pst.executeQuery();
                if (ret.next()) {
                    int userStatus = ret.getInt(1);
                    if (userStatus == 1 || userStatus == 2) {
                        try {
                            auctionInfo ai = getAuctionUtil.getAuctionStatus(bb.getAuctionID());
                            if (ai != null && ai.getAuctionState().equals("active") && ai.getAuctionType().equals("l")) {
                                String tranStr = bb.getAuctionID() + System.currentTimeMillis();
                                String transactionID = getTransID(tranStr);
                                Properties property = new Properties();
                                try {
                                    InputStream in = buyBalls.class.getResourceAsStream("/serverAddress.properties");
                                    property.load(in);
                                    in.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    res.setAuth(-2);
                                    res.setCode(2000);                                          // properties file not found
                                    return res;
                                }
                                String notify_url = property.getProperty("localhostServer") + "/lottery/alipay_notify";
                                double ballPrice = Double.parseDouble(property.getProperty("ballPrice"));
                                double total = ballPrice * bb.getQuantity();
                                DecimalFormat df = new DecimalFormat("0.00");
                                String total_Amount = df.format(total);
                                String alipayStr = AlipayAPPUtil.alipayStr(transactionID, "lottery", "flight upgrade", total_Amount, notify_url);

                                String sql2 = "INSERT INTO ballTransaction (transactionNo, uid, auctionID, certificateNo, totalAmount, ballPrice, quantity, paymentState, timeStamp) VALUES (?,?,?,?,?,?,?,?,?);";
                                pst = conn.prepareStatement(sql2);
                                pst.setString(1, transactionID);
                                pst.setInt(2, uid);
                                pst.setString(3, bb.getAuctionID());
                                pst.setString(4, bb.getCertificateNo());
                                pst.setDouble(5, total);
                                pst.setDouble(6, ballPrice);
                                pst.setInt(7, bb.getQuantity());
                                pst.setInt(8, 0);
                                pst.setString(9, utcTimeStr);
                                pst.executeUpdate();

                                if (userStatus == 1) {
                                    String updateSql = "UPDATE userState set userStatus=2, timeStamp=? WHERE auctionID=? AND uid=? AND certificateNo=?;";
                                    pst = conn.prepareStatement(updateSql);
                                    pst.setString(1, utcTimeStr);
                                    pst.setString(2, bb.getAuctionID());
                                    pst.setInt(3, uid);
                                    pst.setString(4, bb.getCertificateNo());
                                    pst.executeUpdate();
                                }
                                res.setAuth(1);
                                res.setCode(0);
                                res.setMethod("Alipay");
                                res.setSignType("RSA2");
                                res.setSignedStr(alipayStr);
                                res.setTransactionID(transactionID);
                                return res;
                            } else {
                                res.setAuth(-1);
                                res.setCode(1030);                               // error auctionState
                                return res;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            res.setAuth(-2);                                    // auction service fail
                            res.setCode(1060);
                            return res;
                        }
                    } else {
                        res.setAuth(-1);
                        res.setCode(1031);                   // error userStatus
                        return res;
                    }
                } else {
                    res.setAuth(-1);
                    res.setCode(1031);                       // error userStatus
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

    private boolean verifyBuyBallsParam (buyBallsParam bb) {
        try {
            return (bb.getAuctionID() != null && bb.getCertificateNo() != null && bb.getQuantity() > 0);
        } catch (RuntimeException e) {
            return false;
        }
    }

    private String getTransID (String str) {
        char[] charArray = new char[str.length()];
        int j = 0;
        for (int i=0; i<str.length(); i++) {
            if ((str.charAt(i)>='0' && str.charAt(i)<='9'))
                charArray[j++] = str.charAt(i);
        }
        return new String(Arrays.copyOfRange(charArray, 0, j));
    }
}
