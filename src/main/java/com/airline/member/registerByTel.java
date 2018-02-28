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
        try {
            conn = HiKariCPHandler.getConn();
        } catch (SQLException e){
            e.printStackTrace();
            res.setAuth(-2);
            res.setCode(2000);                                   // fail to get mysql connection
            return res;
        }
        try {
            if (!verifyResult) {
                conn.close();
                res.setAuth(-1);
                res.setCode(1000);                               // parameters not correct
                return res;
            } else {
                String searchSql = "SELECT id, password FROM customerAccount WHERE tel_country=? AND tel=?;";
                pst = conn.prepareStatement(searchSql);
                pst.setString(1, rt.getPlatform());
                pst.setString(2, rt.getTel());
                ret = pst.executeQuery();
                if (ret.next()) {
                    conn.close();
                    res.setAuth(-1);
                    res.setCode(1010);                           // tel has been registered
                    return res;
                } else {
                    StringBuffer verifyCode = new StringBuffer("");
                    for(int i=0; i<6; i++){
                        int tmp = (int)Math.floor(Math.random()*10);
                        verifyCode.append(tmp);
                    }
                    String sql = "INSERT INTO preRegister (tel_country, tel, platform, verifyCode, expire) VALUES (?,?,?,?,ADDTIME(utc_timestamp(), '0 00:02:00'));";
                    pst = conn.prepareStatement(sql);
                    pst.setString(1, rt.getTelCountry());
                    pst.setString(2, rt.getTel());
                    pst.setString(3, rt.getPlatform());
                    pst.setString(4, verifyCode.toString());
                    pst.executeUpdate();
                    // TODO
                    // sending msg module
                    //
                    conn.close();
                    res.setAuth(1);
                    res.setCode(0);
                    res.setRegister(true);
                    if (TEXTSWITCH) {
                        res.setVerifyCode(verifyCode.toString());
                    }
                    return res;
                }
            }
        } catch (SQLException e){
            e.printStackTrace();
            res.setAuth(-2);
            res.setCode(2000);                                  // mysql error
            return res;
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
