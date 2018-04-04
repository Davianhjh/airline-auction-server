package com.airline.auction;

import com.airline.tools.HiKariCPHandler;
import com.airline.tools.UTCTimeUtil;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Path("/verifyAddTicket")
public class verifyAddTicket {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public verifyAddTicketRes verifyAdd (verifyAddTicketParam va) {
        verifyAddTicketRes res = new verifyAddTicketRes();
        Connection conn;
        PreparedStatement pst;
        ResultSet ret;
        boolean verifyResult = verifyParam(va);
        if (!verifyResult) {
            res.setAuth(-1);
            res.setCode(1000);                                   // parameters not correct
            return res;
        }

        try {
            conn = HiKariCPHandler.getConn();
        } catch (SQLException e){
            e.printStackTrace();
            res.setAuth(-2);
            res.setCode(2000);                                             // fail to get mysql connection
            return res;
        }
        try {
            String utcTimeStr = UTCTimeUtil.getUTCTimeStr();
            String searchSql = "SELECT sch_id, tel, tel_country FROM searchRecord WHERE verifyCode=? AND platform=? AND expire > ?;";
            pst = conn.prepareStatement(searchSql);
            pst.setString(1, va.getVerifyCode());
            pst.setString(2, va.getPlatform());
            pst.setString(3, utcTimeStr);
            ret = pst.executeQuery();
            if (ret.next()) {
                int search_id = ret.getInt(1);
                if (!ret.getString(2).equals(va.getTel()) || !ret.getString(3).equals(va.getTelCountry())) {
                    res.setAuth(-1);
                    res.setCode(1014);                                     // verify not correct
                    return res;
                } else {
                    String updateSql = "UPDATE searchRecord set verifyCode=null WHERE sch_id=?";
                    pst = conn.prepareStatement(updateSql);
                    pst.setInt(1, search_id);
                    pst.executeUpdate();

                    res.setAuth(1);
                    res.setCode(0);
                    res.setVerify(1);
                    return res;
                }
            } else {
                res.setAuth(-1);
                res.setCode(1014);                                 // verifyCode not correct
                return res;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            res.setAuth(-2);
            res.setCode(2000);                                     // mysql error
            return res;
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean verifyParam (verifyAddTicketParam va) {
        try {
            return va.getTel() != null && va.getTelCountry() != null && va.getVerifyCode() != null && va.getPlatform() != null;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
