package com.airline.member;

import com.airline.tools.HiKariCPHandler;
import com.airline.tools.UTCTimeUtil;
import com.airline.tools.tokenHandler;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Path("/member/verifyQuickAccess")
public class verifyQuickAccess {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public verifyQuickAccessRes verifyLogin (@QueryParam("verifyCode") String verifyCode, @QueryParam("platform") String platform) {
        verifyQuickAccessRes res = new verifyQuickAccessRes();
        Connection conn;
        PreparedStatement pst;
        ResultSet ret, ret2;
        if (verifyCode == null || platform == null) {
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
            String searchSql = "SELECT uid, username FROM quickAccess WHERE verifyCode=? AND platform=? AND expire > ?;";
            pst = conn.prepareStatement(searchSql);
            pst.setString(1, verifyCode);
            pst.setString(2, platform);
            pst.setString(3, utcTimeStr);
            ret = pst.executeQuery();
            if (ret.next()) {
                int uid = ret.getInt(1);
                String userName = ret.getString(2);
                String token = tokenHandler.createJWT(String.valueOf(uid), userName, platform, 7 * 24 * 3600 * 1000);
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
                pst.setString(2, platform);
                pst.setInt(3, uid);
                pst.executeUpdate();

                String deleteSql = "DELETE FROM quickAccess WHERE verifyCode=? AND platform=?;";
                pst = conn.prepareStatement(deleteSql);
                pst.setString(1, verifyCode);
                pst.setString(2, platform);
                pst.executeUpdate();

                res.setAuth(1);
                res.setCode(0);
                res.setName(userName);
                res.setToken(token);
                return res;
            } else {
                res.setAuth(-1);
                res.setCode(1020);                                 // verify code not correct
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
}
