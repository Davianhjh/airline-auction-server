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

@Path("/member/quickAccess")
public class quickAccess {
    private static final boolean TEXTSWITCH = true;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public quickAccessRes quickAccessLogin (quickAccessParam qa) {
        quickAccessRes res = new quickAccessRes();
        Connection conn;
        PreparedStatement pst;
        ResultSet ret;
        boolean verifyResult = verifyQuickAccessParams(qa);
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
            res.setCode(2000);                                   // fail to get mysql connection
            return res;
        }
        try {
            String searchSql = "SELECT id, password, username, cnid_name FROM customerAccount WHERE tel_country=? AND tel=?;";
            pst = conn.prepareStatement(searchSql);
            pst.setString(1, qa.getTelCountry());
            pst.setString(2, qa.getTel());
            ret = pst.executeQuery();
            if (ret.next()) {
                int uid = ret.getInt(1);
                String password = ret.getString(2);
                String userName = ret.getString(3);
                String cnid_name = ret.getString(4);
                StringBuffer verifyCode = new StringBuffer("");
                for(int i=0; i<6; i++){
                    int tmp = (int)Math.floor(Math.random()*10);
                    verifyCode.append(tmp);
                }
                String insertSql = "INSERT INTO quickAccess (uid, tel_country, tel, username, password, platform, verifyCode, expire) VALUES (?,?,?,?,?,?,?,ADDTIME(utc_timestamp(), '0 00:02:00'));";
                pst = conn.prepareStatement(insertSql);
                pst.setInt(1, uid);
                pst.setString(2, qa.getTelCountry());
                pst.setString(3, qa.getTel());
                pst.setString(4, cnid_name == null ? userName : cnid_name);
                pst.setString(5, password);
                pst.setString(6, qa.getPlatform());
                pst.setString(7, verifyCode.toString());
                pst.executeUpdate();
                // TODO
                // sending msg module
                //
                conn.close();
                res.setAuth(1);
                res.setCode(0);
                res.setAccess(1);
                if (TEXTSWITCH) {
                    res.setVerifyCode(verifyCode.toString());
                }
                return res;

            } else {
                res.setAuth(-1);
                res.setCode(1020);                              // user not found
                return res;
            }
        } catch (SQLException e){
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

    private boolean verifyQuickAccessParams (quickAccessParam qa) {
        try {
            return qa.getTel() != null && qa.getTelCountry() != null && qa.getPlatform() != null;
        } catch (RuntimeException e){
            return false;
        }
    }
}
