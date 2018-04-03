package com.airline.member;

import com.airline.tools.HiKariCPHandler;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Path("/member/tel/retrievePassword")
public class retrievePwdByTel {
    private static final boolean TEXTSWITCH = true;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public retrievePwdByTelRes retrieveByTel (retrievePwdByTelParam rt) {
        retrievePwdByTelRes res = new retrievePwdByTelRes();
        Connection conn;
        PreparedStatement pst;
        ResultSet ret;
        boolean verifyResult = verifyRetrieveParam(rt);
        if (!verifyResult) {
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
            String searchSql = "SELECT id FROM customerAccount WHERE tel=? AND tel_country=?";
            pst = conn.prepareStatement(searchSql);
            pst.setString(1, rt.getTel());
            pst.setString(2, rt.getTelCountry());
            ret = pst.executeQuery();
            if (ret.next()) {
                int uid = ret.getInt(1);
                StringBuffer verifyCode = new StringBuffer("");
                for(int i=0; i<6; i++){
                    int tmp = (int)Math.floor(Math.random()*10);
                    verifyCode.append(tmp);
                }
                String insertSql = "INSERT INTO preRegister (uid, tel, tel_country, platform, verifyCode, expire) VALUES (?,?,?,?,?,ADDTIME(utc_timestamp(), '0 00:02:00'))";
                pst = conn.prepareStatement(insertSql);
                pst.setInt(1, uid);
                pst.setString(2, rt.getTel());
                pst.setString(3, rt.getTelCountry());
                pst.setString(4, rt.getPlatform());
                pst.setString(5, verifyCode.toString());
                pst.executeUpdate();

                res.setAuth(1);
                res.setCode(0);
                res.setRetrieve(1);
                if (TEXTSWITCH) {
                    res.setVerifyCode(verifyCode.toString());
                }
                return res;
            } else {
                res.setAuth(-1);
                res.setCode(1028);                           // not registered
                return res;
            }
        } catch (SQLException e) {
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

    private boolean verifyRetrieveParam (retrievePwdByTelParam rt) {
        try {
            return rt.getTel() != null && rt.getTelCountry() != null && rt.getPlatform() != null;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
