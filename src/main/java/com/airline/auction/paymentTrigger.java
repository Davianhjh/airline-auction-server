package com.airline.auction;

import com.airline.tools.HiKariCPHandler;
import com.airline.tools.UTCTimeUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Path("/paymentTrigger")
public class paymentTrigger {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String trigger (paymentTriggerParam pt) {
        Connection conn;
        PreparedStatement pst;
        ResultSet ret;
        boolean verifyResult = verifyPaymentTriggerParam(pt);
        if (!verifyResult) {
            return "error";
        }

        try {
            conn = HiKariCPHandler.getConn();
        } catch (SQLException e){
            return "error";
        }
        try {
            JSONArray winner = pt.getWinner();
            for (Object item: winner) {
                JSONObject tmp = (JSONObject) item;
                String searchSql = "SELECT paymentState FROM tradeRecord WHERE auctionID=? AND uid=? AND certificateNo=?";
                pst = conn.prepareStatement(searchSql);
                pst.setString(1, pt.getAuction());
                pst.setInt(2, tmp.getIntValue("uid"));
                pst.setString(3, tmp.getString("passenger"));
                ret = pst.executeQuery();
                if (ret.next() && ret.getInt(1) == 1);
                else {
                    String updateSql = "UPDATE customerAccount set credit=credit+1 WHERE id=?";
                    pst = conn.prepareStatement(updateSql);
                    pst.setInt(1, tmp.getIntValue("uid"));
                    pst.executeUpdate();

                    // for banned output
                    String utcStr = UTCTimeUtil.getUTCTimeStr();
                    System.out.println("Banned ID: " + tmp.getIntValue("uid"));
                    System.out.println("Banned Time: " + utcStr);
                    System.out.println("Unpaid auction: " + pt.getAuction());
                    System.out.println("certificate: " + tmp.getString("passenger"));
                }
            }
            return "success";
        } catch (SQLException e){
            return "error";
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean verifyPaymentTriggerParam(paymentTriggerParam pt) {
        try {
            return pt.getAuction() != null && pt.getWinner() != null;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
