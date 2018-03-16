package com.airline.poker;

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

@Path("/buyCards")
public class buyCards {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public createBillRes buyCards (@Context HttpHeaders hh, buyCardsParam bc) {
        MultivaluedMap<String, String> header = hh.getRequestHeaders();
        String AgiToken = header.getFirst("token");
        createBillRes res = new createBillRes();
        Connection conn;
        PreparedStatement pst;
        ResultSet ret, ret2;
        boolean verifyResult = verifyBuyCardsParam(bc);
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
                String searchSql = "SELECT card FROM cardTransaction WHERE uid=? AND auctionID=? AND certificateNo=? AND paymentState=1 ORDER BY timeStamp DESC;";
                pst = conn.prepareStatement(searchSql);
                pst.setInt(1, uid);
                pst.setString(2, bc.getAuctionID());
                pst.setString(3, bc.getCertificateNo());
                ret2 = pst.executeQuery();
                String[] cards = bc.getCards().split(",");
                boolean verifyCard;
                if (ret2.next())
                    verifyCard = verifyCardSequence(cards, ret2.getString(1).split(","));
                else verifyCard = verifyCardSequence(cards, null);
                if (verifyCard) {
                    try {
                        auctionInfo ai = getAuctionUtil.getAuctionStatus(bc.getAuctionID());
                        if (ai != null && ai.getAuctionState().equals("active") && ai.getAuctionType().equals("p")) {
                            String total_Amount = getTotalAmount(cards);
                            if (total_Amount == null) {
                                conn.close();
                                res.setAuth(-2);
                                res.setCode(2000);                              // mysql error
                                return res;
                            } else {
                                String tranStr = bc.getAuctionID() + System.currentTimeMillis();
                                String transactionID = getTransID(tranStr);
                                String notify_url = "http://220.202.15.42:10020/poker/alipay_notify";
                                String alipayStr = AlipayAPPUtil.alipayStr(transactionID, "poker", "flight upgrade", total_Amount, notify_url);

                                String insertSql = "INSERT INTO cardTransaction (transactionNo, uid, auctionID, certificateNo, totalAmount, card, paymentState) VALUES (?,?,?,?,?,?,?);";
                                pst = conn.prepareStatement(insertSql);
                                pst.setString(1, transactionID);
                                pst.setInt(2, uid);
                                pst.setString(3, bc.getAuctionID());
                                pst.setString(4, bc.getCertificateNo());
                                pst.setDouble(5, Double.parseDouble(total_Amount));
                                pst.setString(6, bc.getCards());
                                pst.setInt(7, 0);
                                pst.executeUpdate();

                                conn.close();
                                res.setAuth(1);
                                res.setCode(0);
                                res.setMethod("Alipay");
                                res.setSignType("RSA2");
                                res.setSignedStr(alipayStr);
                                res.setTransactionID(transactionID);
                                return res;
                            }
                        } else {
                            conn.close();
                            res.setAuth(-2);
                            res.setCode(1030);                               // error auctionState
                            return res;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        conn.close();
                        res.setAuth(-2);                                    // auction service fail
                        res.setCode(1060);
                        return res;
                    }
                } else {
                    conn.close();
                    res.setAuth(-1);
                    res.setCode(1000);                        // parameters not correct
                    return res;
                }
            } else {
                conn.close();
                res.setAuth(-1);
                res.setCode(1020);                           // user not found
                return res;
            }
        } catch (SQLException e){
            e.printStackTrace();
            res.setAuth(-2);
            res.setCode(2000);                               // mysql error
            return res;
        }
    }

    private boolean verifyBuyCardsParam(buyCardsParam bc) {
        try {
            return (bc.getAuctionID() != null && bc.getCertificateNo() != null && bc.getCards() != null);
        } catch (RuntimeException e) {
            return false;
        }
    }

    private boolean verifyCardSequence(String[] cards, String[] lastArray) {
        try {
            if (lastArray == null) {
                for (int j = 0; j < cards.length; j++) {
                    if (Integer.parseInt(cards[j]) != j + 1) {
                        return false;
                    }
                }
            } else {
                int lastTag = Integer.parseInt(lastArray[lastArray.length - 1]);
                for (int i = 0; i < cards.length; i++) {
                    if (Integer.parseInt(cards[i]) != i + 1 + lastTag) {
                        return false;
                    }
                }
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String getTotalAmount(String[] cards) {
        try {
            Properties property = new Properties();
            InputStream in = AlipayAPPUtil.class.getResourceAsStream("/cardPrice.properties");
            property.load(in);
            in.close();
            double sum = 0.0;
            DecimalFormat df = new DecimalFormat("0.00");
            for (String card: cards) {
                double price = Double.parseDouble(property.getProperty("card" + card));
                sum += price;
            }
            return df.format(sum);
        } catch (IOException e) {
            return null;
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
