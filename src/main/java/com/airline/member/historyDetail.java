package com.airline.member;

import com.airline.auction.passengerResult;
import com.airline.lottery.getBallResult;
import com.airline.lottery.refreshBalls;
import com.airline.poker.getCardResult;
import com.airline.poker.refreshCards;
import com.airline.tools.*;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

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
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

@Path("/historyDetail")
public class historyDetail {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public historyDetailRes detail (@Context HttpHeaders hh, historyDetailParam hd) {
        MultivaluedMap<String, String> header = hh.getRequestHeaders();
        String AgiToken = header.getFirst("token");
        historyDetailRes res = new historyDetailRes();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+0:00"));
        Connection conn;
        PreparedStatement pst;
        ResultSet ret, ret2;
        boolean verifyResult = verifyHistoryDetailParams(hd);
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
            DecimalFormat df = new DecimalFormat("0.00");
            String utcTimeStr = UTCTimeUtil.getUTCTimeStr();
            String verifySql = "SELECT id FROM customerToken INNER JOIN customerAccount ON customerToken.uid = customerAccount.id WHERE token = ? and expire > ?;";
            pst = conn.prepareStatement(verifySql);
            pst.setString(1, AgiToken);
            pst.setString(2, utcTimeStr);
            ret = pst.executeQuery();
            if (ret.next()) {
                int uid = ret.getInt(1);
                auctionInfo ai = getAuctionUtil.getAuctionStatus(hd.getAuctionID());
                if (ai != null && ai.getAuctionState() != null && ai.getAuctionState().equals("result")) {
                    switch (ai.getAuctionType()) {
                        case "1" :
                        case "2" :
                            try {
                                passengerResult pr = getAuctionUtil.getBiddingResult(uid, hd.getAuctionID(), hd.getCertificateNo());
                                res.setAuth(1);
                                res.setCode(0);
                                res.setAuctionID(hd.getAuctionID());
                                res.setAuctionState(ai.getAuctionState());
                                res.setAuctionType(ai.getAuctionType());
                                res.setStartTime(ai.getStartTime());
                                res.setEndTime(ai.getEndTime());
                                res.setDescription(ai.getDescription());
                                res.setBiddingTime(pr.getBiddingTime());
                                res.setBiddingPrice(pr.getBiddingPrice());
                                res.setHit(pr.getHit().equals("Y") ? 1:0);
                                if (pr.getHit().equals("Y")) {
                                    res.setTotalA(df.format(pr.getPaymentPrice()));
                                    String sql = "SELECT transactionNo, paymentState, timeStamp FROM tradeRecord WHERE uid=? AND auctionID=? AND certificateNo=?;";
                                    pst = conn.prepareStatement(sql);
                                    pst.setInt(1, uid);
                                    pst.setString(2, hd.getAuctionID());
                                    pst.setString(3, hd.getCertificateNo());
                                    ret2 = pst.executeQuery();
                                    if (ret2.next()) {
                                        JSONArray arr = new JSONArray();
                                        arr.add(ret2.getString(1));
                                        res.setTransactionNo(arr);
                                        res.setPaymentState(ret2.getInt(2));
                                        res.setPaymentTime(ret2.getString(3));
                                    } else {
                                        long paymentDeadline = ai.getEndTime() + 10*60*1000;
                                        if (System.currentTimeMillis() < paymentDeadline) {
                                            res.setPaymentState(0);                         // not paid, within payment deadline
                                        } else res.setPaymentState(-1);                     // not paid, out of payment deadline
                                    }
                                }
                                return res;
                            } catch (Exception e) {
                                res.setAuth(-2);
                                res.setCode(1060);
                                return res;
                            }
                        case "p" :
                            JSONObject response = getCardResult.getPokerResult(hd.getAuctionID(), hd.getCertificateNo(), uid);
                            if (response != null) {
                                res.setAuth(1);
                                res.setCode(0);
                                res.setAuctionID(hd.getAuctionID());
                                res.setAuctionState(ai.getAuctionState());
                                res.setAuctionType(ai.getAuctionType());
                                res.setStartTime(ai.getStartTime());
                                res.setEndTime(ai.getEndTime());
                                res.setDescription(ai.getDescription());
                                res.setPaymentState(1);
                                res.setHit(response.getBoolean("win") ? 1:0);

                                res.setYourCards(response.getJSONArray("cards"));
                                res.setWinnerCards(response.getJSONArray("winner"));
                                String searchSql = "SELECT transactionNo, totalAmount, timeStamp FROM cardTransaction WHERE uid=? AND auctionID=? AND certificateNo=? AND paymentState=1";
                                pst = conn.prepareStatement(searchSql);
                                pst.setInt(1, uid);
                                pst.setString(2, hd.getAuctionID());
                                pst.setString(3, hd.getCertificateNo());
                                ret2 = pst.executeQuery();
                                JSONArray arr = new JSONArray();
                                double total = 0.00;
                                String timeStamp = null;
                                while (ret2.next()) {
                                    arr.add(ret2.getString(1));
                                    total = total + ret2.getDouble(2);
                                    if(timeStamp == null) {
                                        timeStamp = ret2.getString(3).substring(0,19);
                                    }
                                }
                                res.setTransactionNo(arr);
                                res.setTotalC(df.format(total));
                                try {
                                    res.setBiddingTime(Long.toString(sdf.parse(timeStamp).getTime()));
                                } catch (ParseException e) {
                                    res.setAuth(-2);
                                    res.setCode(2000);
                                }
                                return res;
                            } else {
                                res.setAuth(-2);
                                res.setCode(1060);
                                return res;
                            }
                        case "l" :
                            response = getBallResult.getLotteryResult(hd.getAuctionID(), hd.getCertificateNo(), uid);
                            if (response != null) {
                                res.setAuth(1);
                                res.setCode(0);
                                res.setAuctionID(hd.getAuctionID());
                                res.setAuctionState(ai.getAuctionState());
                                res.setAuctionType(ai.getAuctionType());
                                res.setStartTime(ai.getStartTime());
                                res.setEndTime(ai.getEndTime());
                                res.setDescription(ai.getDescription());
                                res.setPaymentState(1);
                                res.setHit(response.getBoolean("isWinner") ? 1:0);

                                res.setYourBalls(response.getJSONArray("tickets"));
                                res.setWinnerBalls(response.getJSONArray("winnerNumbers"));
                                String searchSql = "SELECT transactionNo, totalAmount, timeStamp FROM ballTransaction WHERE uid=? AND auctionID=? AND certificateNo=? AND paymentState=1";
                                pst = conn.prepareStatement(searchSql);
                                pst.setInt(1, uid);
                                pst.setString(2, hd.getAuctionID());
                                pst.setString(3, hd.getCertificateNo());
                                ret2 = pst.executeQuery();
                                JSONArray arr = new JSONArray();
                                double total = 0.00;
                                String timeStamp = null;
                                while (ret2.next()) {
                                    arr.add(ret2.getString(1));
                                    total = total + ret2.getDouble(2);
                                    if(timeStamp == null) {
                                        timeStamp = ret2.getString(3).substring(0,19);
                                    }
                                }
                                res.setTransactionNo(arr);
                                res.setTotalB(df.format(total));
                                try {
                                    res.setBiddingTime(Long.toString(sdf.parse(timeStamp).getTime()));
                                } catch (ParseException e) {
                                    res.setAuth(-2);
                                    res.setCode(2000);
                                }
                                return res;
                            } else {
                                res.setAuth(-2);
                                res.setCode(1060);
                                return res;
                            }
                        default :
                            res.setAuth(-2);
                            res.setCode(2000);                    // shouldn't be here
                            return res;
                    }
                } else if (ai != null && ai.getAuctionState() != null) {
                    switch (ai.getAuctionType()) {
                        case "1" :
                        case "2" :
                            try {
                                passengerResult pr = getAuctionUtil.getBiddingResult(uid, hd.getAuctionID(), hd.getCertificateNo());
                                res.setAuth(1);
                                res.setCode(0);
                                res.setAuctionID(hd.getAuctionID());
                                res.setAuctionState(ai.getAuctionState());
                                res.setAuctionType(ai.getAuctionType());
                                res.setStartTime(ai.getStartTime());
                                res.setEndTime(ai.getEndTime());
                                res.setDescription(ai.getDescription());
                                res.setBiddingTime(pr.getBiddingTime());

                                res.setBiddingPrice(pr.getBiddingPrice());
                                return res;
                            } catch (Exception e) {
                                res.setAuth(-2);
                                res.setCode(1060);
                                return res;
                            }
                        case "p" :
                            JSONObject response = refreshCards.getCards(hd.getAuctionID(), hd.getCertificateNo(), uid);
                            if (response != null) {
                                res.setAuth(1);
                                res.setCode(0);
                                res.setAuctionID(hd.getAuctionID());
                                res.setAuctionState(ai.getAuctionState());
                                res.setAuctionType(ai.getAuctionType());
                                res.setStartTime(ai.getStartTime());
                                res.setEndTime(ai.getEndTime());
                                res.setDescription(ai.getDescription());
                                res.setPaymentState(1);

                                res.setYourCards(response.getJSONArray("cards"));
                                String searchSql = "SELECT sum(totalAmount), timeStamp FROM cardTransaction WHERE uid=? AND auctionID=? AND certificateNo=? AND paymentState=1";
                                pst = conn.prepareStatement(searchSql);
                                pst.setInt(1, uid);
                                pst.setString(2, hd.getAuctionID());
                                pst.setString(3, hd.getCertificateNo());
                                ret = pst.executeQuery();
                                if (ret.next()) {
                                    res.setTotalC(df.format(ret.getDouble(1)));
                                    res.setBiddingTime(ret.getString(2).substring(0,19));
                                }
                                return res;
                            } else {
                                res.setAuth(-2);
                                res.setCode(1060);
                                return res;
                            }
                        case "l" :
                            response = refreshBalls.getBalls(hd.getAuctionID(), hd.getCertificateNo(), uid);
                            if (response != null) {
                                res.setAuth(1);
                                res.setCode(0);
                                res.setAuctionID(hd.getAuctionID());
                                res.setAuctionState(ai.getAuctionState());
                                res.setAuctionType(ai.getAuctionType());
                                res.setStartTime(ai.getStartTime());
                                res.setEndTime(ai.getEndTime());
                                res.setDescription(ai.getDescription());
                                res.setPaymentState(1);

                                res.setYourBalls(response.getJSONArray("tickets"));
                                String searchSql = "SELECT sum(totalAmount), timeStamp FROM ballTransaction WHERE uid=? AND auctionID=? AND certificateNo=? AND paymentState=1";
                                pst = conn.prepareStatement(searchSql);
                                pst.setInt(1, uid);
                                pst.setString(2, hd.getAuctionID());
                                pst.setString(3, hd.getCertificateNo());
                                ret = pst.executeQuery();
                                if (ret.next()) {
                                    res.setTotalB(df.format(ret.getDouble(1)));
                                    res.setBiddingTime(ret.getString(2).substring(0,19));
                                }
                                return res;
                            } else {
                                res.setAuth(-2);
                                res.setCode(1060);
                                return res;
                            }
                        default :
                            res.setAuth(-2);
                            res.setCode(2000);                    // shouldn't be here
                            return res;
                    }
                } else {
                    res.setAuth(-2);
                    res.setCode(1060);
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

    private boolean verifyHistoryDetailParams (historyDetailParam hd) {
        try {
            return hd.getAuctionID() != null && hd.getCertificateNo() != null;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
