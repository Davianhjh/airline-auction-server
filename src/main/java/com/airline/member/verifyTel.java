package com.airline.member;

import com.airline.tools.HiKariCPHandler;
import com.airline.tools.MD5Util;
import com.airline.tools.UTCTimeUtil;
import com.airline.tools.tokenHandler;
import com.mysql.jdbc.Statement;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Path("/member/verifyTel")
public class verifyTel {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public verifyTelRes verifyTel (@QueryParam("verifyCode") String verifyCode, @QueryParam("platform") String platform) {
        verifyTelRes res = new verifyTelRes();
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
            String searchSql = "SELECT tel, tel_country, password FROM preRegister WHERE verifyCode=? AND platform=? AND expire > ?;";
            pst = conn.prepareStatement(searchSql);
            pst.setString(1, verifyCode);
            pst.setString(2, platform);
            pst.setString(3, utcTimeStr);
            ret = pst.executeQuery();
            if (ret.next()) {
                String tel = ret.getString(1);
                String telCountry = ret.getString(2);
                String password = ret.getString(3);
                String sql1 = "SELECT id, username FROM customerAccount WHERE tel=? AND tel_country=?";
                pst = conn.prepareStatement(sql1);
                pst.setString(1, tel);
                pst.setString(2, telCountry);
                ret2 = pst.executeQuery();
                if (ret2.next()) {
                    int uid = ret2.getInt(1);
                    String userName = ret2.getString(2);
                    String token = tokenHandler.createJWT(String.valueOf(uid), userName, platform, 7 * 24 * 3600 * 1000);
                    String updateSql = "UPDATE customerToken SET token=?, expire=ADDTIME(utc_timestamp(), '7 00:00:00'), platform=? WHERE uid=?;";
                    pst = conn.prepareStatement(updateSql);
                    pst.setString(1, token);

                    res.setAuth(1);
                    res.setCode(0);
                    res.setName(userName);
                    res.setToken(token);
                    return res;
                } else {
                    String sql2 = "INSERT INTO customerAccount (tel, tel_country, username, password, platform) VALUES (?,?,?,?,?);";
                    String userName = MD5Util.getMD5(tel).substring(0,10);
                    if (userName == null) {
                        res.setAuth(-2);
                        res.setCode(2000);                                     // MD5 error
                        return res;
                    }
                    pst = conn.prepareStatement(sql2, Statement.RETURN_GENERATED_KEYS);
                    pst.setString(1, tel);
                    pst.setString(2, telCountry);
                    pst.setString(3, userName);
                    pst.setString(4, password);
                    pst.setString(5, platform);
                    pst.executeUpdate();
                    ResultSet rs = pst.getGeneratedKeys();
                    rs.next();
                    int uid = rs.getInt(1);
                    String token = tokenHandler.createJWT(String.valueOf(uid), userName, platform, 7 * 24 * 3600 * 1000);

                    String sql3 = "DELETE FROM preRegister WHERE verifyCode=? AND platform=?;";
                    pst = conn.prepareStatement(sql3);
                    pst.setString(1, verifyCode);
                    pst.setString(2, platform);
                    pst.executeUpdate();

                    String sql4 = "INSERT INTO customerToken (uid, token, platform, expire) VALUES (?,?,?,ADDTIME(utc_timestamp(), '7 00:00:00'));";
                    pst = conn.prepareStatement(sql4);
                    pst.setInt(1, uid);
                    pst.setString(2, token);
                    pst.setString(3, platform);
                    pst.executeUpdate();

                    res.setAuth(1);
                    res.setCode(0);
                    res.setName(userName);
                    res.setToken(token);
                    return res;
                }
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
