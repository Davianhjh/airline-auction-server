package com.airline.member;

import com.airline.tools.HiKariCPHandler;
import com.airline.tools.MD5Util;
import com.airline.tools.UTCTimeUtil;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Path("/member/verifyMail")
public class verifyMail {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public verifyMailRes verifyTel (@QueryParam("verifyCode") String verifyCode, @QueryParam("platform") String platform) {
        verifyMailRes res = new verifyMailRes();
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
            res.setCode(2000);                               // fail to get mysql connection
            return res;
        }
        try {
            String utcTimeStr = UTCTimeUtil.getUTCTimeStr();
            String searchSql = "SELECT email, password FROM preRegister WHERE verifyCode=? AND platform=? AND expire > ?;";
            pst = conn.prepareStatement(searchSql);
            pst.setString(1, verifyCode);
            pst.setString(2, platform);
            pst.setString(3, utcTimeStr);
            ret = pst.executeQuery();
            if (ret.next()) {
                String email = ret.getString(1);
                String password = ret.getString(2);
                String sql1 = "SELECT id FROM customerAccount WHERE email=?";
                pst = conn.prepareStatement(sql1);
                pst.setString(1, email);
                ret2 = pst.executeQuery();
                if (ret2.next()) {
                    conn.close();
                    res.setAuth(1);
                    res.setCode(0);
                    res.setActivated(true);
                    return res;
                } else {
                    String sql2 = "INSERT INTO customerAccount (email, password, username, platform) VALUES (?,?,?,?);";
                    String userName = MD5Util.getMD5(email);
                    if (userName == null) {
                        res.setAuth(-2);
                        res.setCode(2000);                                     // MD5 error
                        return res;
                    }
                    pst = conn.prepareStatement(sql2);
                    pst.setString(1, email);
                    pst.setString(2, password);
                    pst.setString(3, userName.substring(0, 10));
                    pst.setString(4, platform);
                    pst.executeUpdate();

                    String sql3 = "DELETE FROM preRegister WHERE verifyCode=? AND platform=?;";
                    pst = conn.prepareStatement(sql3);
                    pst.setString(1, verifyCode);
                    pst.setString(2, platform);
                    pst.executeUpdate();

                    conn.close();
                    res.setAuth(1);
                    res.setCode(0);
                    res.setActivated(true);
                    return res;
                }
            } else {
                conn.close();
                res.setAuth(-1);
                res.setCode(1020);                             // verify code not correct
                res.setActivated(false);
                return res;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            res.setAuth(-2);
            res.setCode(2000);                                // mysql error
            return res;
        }
    }
}
