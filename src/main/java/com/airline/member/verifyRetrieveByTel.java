package com.airline.member;

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

@Path("/member/tel/verifyRetrieve")
public class verifyRetrieveByTel {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public verifyRetrieveByTelRes verifyRetrieve (verifyRetrieveByTelParam vr) {
        verifyRetrieveByTelRes res = new verifyRetrieveByTelRes();
        Connection conn;
        PreparedStatement pst;
        ResultSet ret;
        boolean verifyResult = verifyRetrieveParam(vr);
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
            res.setCode(2000);                                   // fail to get mysql connection
            return res;
        }

        try {
            String utcTimeStr = UTCTimeUtil.getUTCTimeStr();
            String searchSql = "SELECT uid, tel, tel_country FROM preRegister WHERE verifyCode=? AND platform=? AND expire > ?;";
            pst = conn.prepareStatement(searchSql);
            pst.setString(1, vr.getVerifyCode());
            pst.setString(2, vr.getPlatform());
            pst.setString(3, utcTimeStr);
            ret = pst.executeQuery();
            if (ret.next()) {
                int uid = ret.getInt(1);
                if (uid == 0 || !vr.getTel().equals(ret.getString(2)) || !vr.getTelCountry().equals(ret.getString(3))) {
                    res.setAuth(-1);
                    res.setCode(1029);                             // verify not correct
                    return res;
                } else {
                    String updateSql = "UPDATE customerAccount set password=? WHERE id=?";
                    pst = conn.prepareStatement(updateSql);
                    pst.setString(1, vr.getVerifyCode());
                    pst.setInt(2, uid);
                    pst.executeUpdate();

                    String deleteSql = "DELETE FROM preRegister WHERE verifyCode=? AND platform=?;";
                    pst = conn.prepareStatement(deleteSql);
                    pst.setString(1, vr.getVerifyCode());
                    pst.setString(2, vr.getPlatform());
                    pst.executeUpdate();

                    res.setAuth(1);
                    res.setCode(0);
                    res.setVerify(1);
                    return res;
                }
            } else {
                res.setAuth(-1);
                res.setCode(1029);                                 // verify not correct
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

    private boolean verifyRetrieveParam (verifyRetrieveByTelParam rt) {
        try {
            return rt.getTel() != null && rt.getTelCountry() != null && rt.getVerifyCode() != null && rt.getPlatform() != null;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
