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
    public verifyMailRes verifyMail (@QueryParam("verifyCode") String verifyCode, @QueryParam("platform") String platform) {
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
            String searchSql = "SELECT uid, email, password FROM preRegister WHERE verifyCode=? AND platform=? AND expire > ?;";
            pst = conn.prepareStatement(searchSql);
            pst.setString(1, verifyCode);
            pst.setString(2, platform);
            pst.setString(3, utcTimeStr);
            ret = pst.executeQuery();
            if (ret.next()) {
                int uid = ret.getInt(1);
                String email = ret.getString(2);
                String password = ret.getString(3);
                String sql1 = "SELECT id from customerAccount WHERE email=?";
                pst = conn.prepareStatement(sql1);
                pst.setString(1, email);
                if (ret.next()) {
                    res.setAuth(-1);
                    res.setCode(1023);                          // email has been bind (by someone else)
                    return res;
                }
                else if (uid != 0 && email != null) {
                    String sql2 = "SELECT email FROM customerAccount where id=?";
                    pst = conn.prepareStatement(sql2);
                    pst.setInt(1, uid);
                    ret2 = pst.executeQuery();
                    if (ret2.next() && ret2.getString(1) == null) {
                        String sql3 = "UPDATE customerAccount set email=? WHERE id=?";
                        pst = conn.prepareStatement(sql3);
                        pst.setString(1, email);
                        pst.setInt(2, uid);
                        pst.executeUpdate();
                    } else {
                        res.setAuth(-1);
                        res.setCode(1023);                       // email has been bind (by yourself)
                        return res;
                    }
                } else if (uid == 0 && email != null) {
                    String sql2 = "INSERT INTO customerAccount (email, password, username, platform) VALUES (?,?,?,?);";
                    String userName = MD5Util.getMD5(email);
                    if (userName == null) {
                        res.setAuth(-2);
                        res.setCode(2000);                       // MD5 error
                        return res;
                    }
                    pst = conn.prepareStatement(sql2);
                    pst.setString(1, email);
                    pst.setString(2, password);
                    pst.setString(3, userName.substring(0,10));
                    pst.setString(4, platform);
                    pst.executeUpdate();
                } else {
                    res.setAuth(-2);
                    res.setCode(2000);                           // mysql data error
                    return res;
                }
                String sql3 = "DELETE FROM preRegister WHERE verifyCode=? AND platform=?;";
                pst = conn.prepareStatement(sql3);
                pst.setString(1, verifyCode);
                pst.setString(2, platform);
                pst.executeUpdate();

                res.setAuth(1);
                res.setCode(0);
                res.setActivated(true);
                return res;
            } else {
                res.setAuth(-1);
                res.setCode(1014);                             // verify code not correct
                return res;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            res.setAuth(-2);
            res.setCode(2000);                                // mysql error
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
