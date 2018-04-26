package com.airline.member;

import com.airline.tools.HiKariCPHandler;
import com.airline.tools.msgSendUtil;
import org.mindrot.jbcrypt.BCrypt;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

@Path("/member/registerByTel")
public class registerByTel {
    private static final boolean TEXTSWITCH = true;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public registerByTelRes register (registerByTelParam rt) {
        registerByTelRes res = new registerByTelRes();
        Connection conn;
        PreparedStatement pst;
        ResultSet ret;
        boolean verifyResult = verifyRegisterByTelParams(rt);
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
            String searchSql = "SELECT id FROM customerAccount WHERE tel_country=? AND tel=?;";
            pst = conn.prepareStatement(searchSql);
            pst.setString(1, rt.getTelCountry());
            pst.setString(2, rt.getTel());
            ret = pst.executeQuery();
            if (ret.next()) {
                if (ret.getString(1) != null) {
                    res.setAuth(-1);
                    res.setCode(1011);                           // tel has been registered, use quick access
                } else {
                    res.setAuth(-1);
                    res.setCode(1010);                           // tel has been registered, probably not yours
                }
                return res;
            } else {
                StringBuffer verifyCode = new StringBuffer("");
                for(int i=0; i<6; i++){
                    int tmp = (int)Math.floor(Math.random()*10);
                    verifyCode.append(tmp);
                }
                String password;
                try {
                    password = rt.getPassword();
                } catch (RuntimeException e) {
                    password = null;
                }
                String sql = "INSERT INTO preRegister (tel_country, tel, password, platform, verifyCode, expire) VALUES (?,?,?,?,?,ADDTIME(utc_timestamp(), '0 00:10:00'));";
                pst = conn.prepareStatement(sql);
                pst.setString(1, rt.getTelCountry());
                pst.setString(2, rt.getTel());
                pst.setString(3, password == null ? null : BCrypt.hashpw(password, BCrypt.gensalt()));
                pst.setString(4, rt.getPlatform());
                pst.setString(5, verifyCode.toString());
                pst.executeUpdate();
                // TODO
                // sending msg module
                String smsText="【Agiview竞拍平台】您的验证码是" + verifyCode.toString() + "，10分钟内有效，请勿向任何人泄露。";
                res.setAuth(1);
                res.setCode(0);
                res.setRegister(1);
                if (TEXTSWITCH) {
                    res.setVerifyCode(verifyCode.toString());
                    return res;
                } else if (msgSendUtil.sendMsg(rt.getTel(), smsText) == 0){
                    return res;
                } else {
                    res.setAuth(-2);
                    res.setCode(1070);
                    return res;
                }
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

    private boolean verifyRegisterByTelParams(registerByTelParam rt){
        try {
            return rt.getTel() != null && rt.getTelCountry() != null && rt.getPlatform() != null;
        } catch (RuntimeException e){
            return false;
        }
    }
}