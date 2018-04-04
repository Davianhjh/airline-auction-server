package com.airline.member;

import com.airline.tools.HiKariCPHandler;
import com.airline.tools.MD5Util;
import com.airline.tools.UTCTimeUtil;
import com.airline.tools.tokenHandler;
import com.mysql.jdbc.Statement;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Path("/member/verifyTel")
public class verifyTel {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public verifyTelRes verifyTel (verifyTelParam vt) {
        verifyTelRes res = new verifyTelRes();
        Connection conn;
        PreparedStatement pst;
        ResultSet ret, ret2;
        boolean verifyResult = verifyParam(vt);
        if (!verifyResult) {
            res.setAuth(-1);
            res.setCode(1000);                                             // parameters not correct
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
            String searchSql = "SELECT tel, tel_country, password FROM preRegister WHERE verifyCode=? AND platform=? AND expire > ?;";
            pst = conn.prepareStatement(searchSql);
            pst.setString(1, vt.getVerifyCode());
            pst.setString(2, vt.getPlatform());
            pst.setString(3, utcTimeStr);
            ret = pst.executeQuery();
            if (ret.next()) {
                String password = ret.getString(3);
                if (!ret.getString(1).equals(vt.getTel()) || !ret.getString(2).equals(vt.getTelCountry())) {
                    res.setAuth(-1);
                    res.setCode(1014);                                     // verify not correct
                    return res;
                }
                String sql1 = "SELECT id FROM customerAccount WHERE tel=? AND tel_country=?";
                pst = conn.prepareStatement(sql1);
                pst.setString(1, vt.getTel());
                pst.setString(2, vt.getTelCountry());
                ret2 = pst.executeQuery();
                if (ret2.next()) {
                    res.setAuth(-1);
                    res.setCode(1022);                                     // tel has been bind (by someone else)
                    return res;
                } else {
                    String userName = MD5Util.getMD5(vt.getTel());
                    if (userName == null) {
                        res.setAuth(-2);
                        res.setCode(2000);                                 // MD5 error
                        return res;
                    }
                    String sql2 = "INSERT INTO customerAccount (tel, tel_country, password, username, platform) VALUES (?,?,?,?,?);";
                    pst = conn.prepareStatement(sql2, Statement.RETURN_GENERATED_KEYS);
                    pst.setString(1, vt.getTel());
                    pst.setString(2, vt.getTelCountry());
                    pst.setString(3, password);
                    pst.setString(4, userName.substring(0, 10));
                    pst.setString(5, vt.getPlatform());
                    pst.executeUpdate();
                    ResultSet rs = pst.getGeneratedKeys();
                    rs.next();
                    int uid = rs.getInt(1);
                    String token = tokenHandler.createJWT(String.valueOf(uid), userName.substring(0,10), vt.getPlatform(), 7 * 24 * 3600 * 1000);

                    String sql3 = "INSERT INTO customerToken (uid, token, platform, expire) VALUES (?,?,?,ADDTIME(utc_timestamp(), '7 00:00:00'));";
                    pst = conn.prepareStatement(sql3);
                    pst.setInt(1, uid);
                    pst.setString(2, token);
                    pst.setString(3, vt.getPlatform());
                    pst.executeUpdate();

                    String sql4 = "DELETE FROM preRegister WHERE verifyCode=? AND platform=?;";
                    pst = conn.prepareStatement(sql4);
                    pst.setString(1, vt.getVerifyCode());
                    pst.setString(2, vt.getPlatform());
                    pst.executeUpdate();

                    res.setAuth(1);
                    res.setCode(0);
                    res.setName(userName.substring(0,10));
                    res.setToken(token);
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

    private boolean verifyParam (verifyTelParam vt) {
        try {
            return vt.getTel() != null && vt.getTelCountry() != null && vt.getVerifyCode() != null && vt.getPlatform() != null;
        } catch (RuntimeException e) {
            return false;
        }
    }
}