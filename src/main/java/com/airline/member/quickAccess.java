package com.airline.member;

import com.airline.tools.HiKariCPHandler;
import com.airline.tools.msgSendUtil;

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
    private static final boolean TEXTSWITCH = false;

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
            String searchSql = "SELECT id, password, username, cnid_name, credit FROM customerAccount WHERE tel_country=? AND tel=?;";
            pst = conn.prepareStatement(searchSql);
            pst.setString(1, qa.getTelCountry());
            pst.setString(2, qa.getTel());
            ret = pst.executeQuery();
            if (ret.next()) {
                int uid = ret.getInt(1);
                String password = ret.getString(2);
                String userName = ret.getString(3);
                String cnid_name = ret.getString(4);
                int credit = ret.getInt(5);
                if (credit >= 2) {
                    res.setAuth(-2);
                    res.setCode(1080);                          // you have been banned due to hit without paying
                    return res;
                }
                StringBuffer verifyCode = new StringBuffer("");
                for(int i=0; i<6; i++){
                    int tmp = (int)Math.floor(Math.random()*10);
                    verifyCode.append(tmp);
                }
                String insertSql = "INSERT INTO quickAccess (uid, tel_country, tel, username, password, platform, verifyCode, expire) VALUES (?,?,?,?,?,?,?,ADDTIME(utc_timestamp(), '0 00:10:00'));";
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
                String smsText="【Agiview竞拍平台】您的验证码是" + verifyCode.toString() + "，10分钟内有效，请勿向任何人泄露。";
                res.setAuth(1);
                res.setCode(0);
                res.setAccess(1);
                if (TEXTSWITCH) {
                    res.setVerifyCode(verifyCode.toString());
                    return res;
                }
                else if (msgSendUtil.sendMsg(qa.getTel(), smsText) == 0){
                    return res;
                } else {
                    res.setAuth(-2);
                    res.setCode(1090);
                    return res;
                }
            } else {
                res.setAuth(-1);
                res.setCode(1018);                              // user not registered
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
