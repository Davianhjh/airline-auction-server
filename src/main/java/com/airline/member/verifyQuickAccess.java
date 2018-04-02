package com.airline.member;

import com.airline.tools.HiKariCPHandler;
import com.airline.tools.UTCTimeUtil;
import com.airline.tools.tokenHandler;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Path("/member/verifyQuickAccess")
public class verifyQuickAccess {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public verifyQuickAccessRes verifyLogin (verifyQuickAccessParam vq) {
        verifyQuickAccessRes res = new verifyQuickAccessRes();
        Connection conn;
        PreparedStatement pst;
        ResultSet ret, ret2;
        boolean verifyResult = verifyParam(vq);
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
            String utcTimeStr = UTCTimeUtil.getUTCTimeStr();
            String searchSql = "SELECT uid, tel, tel_country, password, username FROM quickAccess WHERE verifyCode=? AND platform=? AND expire > ?;";
            pst = conn.prepareStatement(searchSql);
            pst.setString(1, vq.getVerifyCode());
            pst.setString(2, vq.getPlatform());
            pst.setString(3, utcTimeStr);
            ret = pst.executeQuery();
            if (ret.next()) {
                int uid = ret.getInt(1);
                String tel = ret.getString(2);
                String telCountry = ret.getString(3);
                String password = ret.getString(4);
                String userName = ret.getString(5);
                if (!tel.equals(vq.getTel()) || !telCountry.equals(vq.getTelCountry())) {
                    res.setAuth(-1);
                    res.setCode(1025);                                 // verify not correct
                    return res;
                }
                String token = tokenHandler.createJWT(String.valueOf(uid), userName, vq.getPlatform(), 7 * 24 * 3600 * 1000);
                String sql1 = "SELECT tid from customerToken WHERE uid=?";
                pst = conn.prepareStatement(sql1);
                pst.setInt(1, uid);
                ret2 = pst.executeQuery();

                if (ret2.next()) {
                    String updateSql = "UPDATE customerToken SET token=?, expire=ADDTIME(utc_timestamp(), '7 00:00:00'), platform=? WHERE uid=?;";
                    pst = conn.prepareStatement(updateSql);
                } else {
                    String insertSql = "INSERT INTO customerToken (token, platform, uid, expire) VALUES (?,?,?,ADDTIME(utc_timestamp(), '7 00:00:00'));";
                    pst = conn.prepareStatement(insertSql);
                }
                pst.setString(1, token);
                pst.setString(2, vq.getPlatform());
                pst.setInt(3, uid);
                pst.executeUpdate();

                String deleteSql = "DELETE FROM quickAccess WHERE verifyCode=? AND platform=?;";
                pst = conn.prepareStatement(deleteSql);
                pst.setString(1, vq.getVerifyCode());
                pst.setString(2, vq.getPlatform());
                pst.executeUpdate();

                res.setAuth(1);
                res.setCode(0);
                res.setName(userName);
                res.setToken(token);
                res.setPwdSetTag(password == null ? 1 : 0);
                return res;
            } else {
                res.setAuth(-1);
                res.setCode(1025);                                 // verify code not correct
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

    private boolean verifyParam (verifyQuickAccessParam vq) {
        try {
            return vq.getTel() != null && vq.getTelCountry() != null && vq.getVerifyCode() != null && vq.getPlatform() != null;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
