package com.airline.auction;

import com.airline.tools.HiKariCPHandler;
import com.airline.tools.UTCTimeUtil;
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

@Path("/verifyAlipayBill")
public class verifyAlipayBill {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public verifyAlipayBillRes verify (@Context HttpHeaders hh, JSONObject testBody) {
        MultivaluedMap<String, String> header = hh.getRequestHeaders();
        String AgiToken = header.getFirst("token");
        verifyAlipayBillRes res = new verifyAlipayBillRes();
        Connection conn;
        PreparedStatement pst;
        ResultSet ret;

        if (AgiToken == null) {
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
                String transactionID = testBody.getString("transactionID");
                if (transactionID == null) {                          // payment not escaped
                    conn.close();
                    res.setAuth(1);
                    res.setCode(0);
                    res.setVerify(1);
                    return res;
                } else {                                              // payment escaped
                    String updateSql = "UPDATE tradeRecord SET paymentState=1 WHERE transactionNo=?;";
                    pst = conn.prepareStatement(updateSql);
                    pst.setString(1, transactionID);
                    pst.executeUpdate();
                    conn.close();
                    res.setAuth(1);
                    res.setCode(0);
                    res.setVerify(1);
                    return res;
                }
            } else {
                conn.close();
                res.setAuth(-1);
                res.setCode(1020);                                      // user not found
                return res;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            res.setAuth(-2);
            res.setCode(2000);                                          // mysql error
            return res;
        }
    }
}
