package com.airline.auction;

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

@Path("/pageAlipayBill")
public class pageAlipayBill {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_HTML)
    public String buyBalls (@Context HttpHeaders hh, createAlipayBillParam ca) {
        MultivaluedMap<String, String> header = hh.getRequestHeaders();
        String AgiToken = header.getFirst("token");
        Connection conn;
        PreparedStatement pst;
        ResultSet ret;
        boolean verifyResult = verifyCreateAlipayBillParam(ca);
        if (AgiToken == null || !verifyResult) {
            return "1000";                                              // parameters not correct
        }

        try {
            conn = HiKariCPHandler.getConn();
        } catch (SQLException e){
            return "2000";                                              // fail to get mysql connection
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
                passengerResult result;
                try {
                    result = getAuctionUtil.getBiddingResult(uid, ca.getAuctionID(), ca.getCertificateNo());
                } catch (Exception e) {
                    return "1060";                                      // auction service fail
                }
                if (!result.getAuctionState().equals("result") || (!result.getAuctionType().equals("1") && !result.getAuctionType().equals("2")) || !result.getHit().equals("Y") || result.getBiddingPrice() <= 0) {
                    return "1030";                                      // error auctionState
                }
                DecimalFormat df = new DecimalFormat("0.00");
                String biddingPrice = df.format(result.getBiddingPrice());
                String tranStr = ca.getAuctionID() + System.currentTimeMillis();
                String transactionID = getTransID(tranStr);
                Properties property = new Properties();
                try {
                    InputStream in = createAlipayBill.class.getResourceAsStream("/serverAddress.properties");
                    property.load(in);
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return "2000";                                       // properties file not found
                }
                String notify_url = property.getProperty("localhostServer") + "/auction/alipay_notify";
                String formStr = AlipayUtil.alipayPageStr(transactionID, "auction", "flight upgrade", biddingPrice, notify_url);
                if (formStr == null) {
                    return "2000";                                       // alipay sdk error
                }
                String sql2 = "INSERT INTO tradeRecord (transactionNo, uid, auctionID, certificateNo, totalAmount, paymentState, timeStamp) VALUES (?,?,?,?,?,?,?);";
                pst = conn.prepareStatement(sql2);
                pst.setString(1, transactionID);
                pst.setInt(2, uid);
                pst.setString(3, ca.getAuctionID());
                pst.setString(4, ca.getCertificateNo());
                pst.setDouble(5, result.getPaymentPrice());
                pst.setInt(6, 0);
                pst.setString(7, utcTimeStr);
                pst.executeUpdate();

                return formStr;
            } else {
                return "1020";                                           // user not found
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "2000";                                               // mysql error
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean verifyCreateAlipayBillParam (createAlipayBillParam ca) {
        try {
            return ((ca.getAuctionID() != null) && (ca.getCertificateNo() != null));
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