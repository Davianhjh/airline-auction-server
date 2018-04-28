package com.airline.member;

import com.airline.tools.HiKariCPHandler;
import com.airline.tools.UTCTimeUtil;
import com.airline.tools.msgSendUtil;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Path("/member/setTel")
public class setTel {
    private static final boolean TEXTSWITCH = true;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public setTelRes reviseTel (@Context HttpHeaders hh, setTelParam st) {
        MultivaluedMap<String, String> header = hh.getRequestHeaders();
        String AgiToken = header.getFirst("token");
        setTelRes res = new setTelRes();
        Connection conn;
        PreparedStatement pst;
        ResultSet ret;
        boolean verifyResult = verifySetTelParam(st);
        if (AgiToken == null | !verifyResult) {
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
            String verifySql = "SELECT uid, tel, email FROM customerToken INNER JOIN customerAccount ON customerToken.uid = customerAccount.id WHERE token = ? and expire > ?;";
            pst = conn.prepareStatement(verifySql);
            pst.setString(1, AgiToken);
            pst.setString(2, utcTimeStr);
            ret = pst.executeQuery();
            if (ret.next()) {
                int uid = ret.getInt(1);
                String tel = ret.getString(2);
                String email = ret.getString(3);
                if (tel != null || email == null) {
                    res.setAuth(-1);
                    res.setCode(1022);                          // tel has been set
                    return res;
                }
                String searchSql = "SELECT id FROM customerAccount WHERE tel_country=? AND tel=?;";
                pst = conn.prepareStatement(searchSql);
                pst.setString(1, st.getTelCountry());
                pst.setString(2, st.getTel());
                ret = pst.executeQuery();
                if (ret.next()) {
                    res.setAuth(-1);
                    res.setCode(1010);                          // tel registered
                    return res;
                } else {
                    StringBuffer verifyCode = new StringBuffer("");
                    for(int i=0; i<6; i++){
                        int tmp = (int)Math.floor(Math.random()*10);
                        verifyCode.append(tmp);
                    }
                    String sql = "INSERT INTO preRegister (uid, tel_country, tel, platform, verifyCode, expire) VALUES (?,?,?,?,?,ADDTIME(utc_timestamp(), '0 00:10:00'));";
                    pst = conn.prepareStatement(sql);
                    pst.setInt(1, uid);
                    pst.setString(2, st.getTelCountry());
                    pst.setString(3, st.getTel());
                    pst.setString(4, st.getPlatform());
                    pst.setString(5, verifyCode.toString());
                    pst.executeUpdate();
                    // TODO
                    // sending msg module
                    String smsText="【Agiview竞拍平台】您的验证码是" + verifyCode.toString() + "，10分钟内有效，请勿向任何人泄露。";
                    res.setAuth(1);
                    res.setCode(0);
                    res.setBind(1);
                    if (TEXTSWITCH) {
                        res.setVerifyCode(verifyCode.toString());
                        return res;
                    } else if (msgSendUtil.sendMsg(st.getTel(), smsText) == 0){
                        return res;
                    } else {
                        res.setAuth(-2);
                        res.setCode(1090);
                        return res;
                    }
                }
            } else {
                res.setAuth(-1);
                res.setCode(1020);                              // user not found
                return res;
            }
        } catch (SQLException e) {
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

    private boolean verifySetTelParam (setTelParam st) {
        try {
            return st.getTel() != null && st.getTelCountry() != null && st.getPlatform() != null;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
