package com.airline.member;

import com.airline.tools.HiKariCPHandler;
import com.airline.tools.mailSendUtil;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

@Path("/member/retrievePassword")
public class retrievePassword {
    private static final boolean TEXTSWITCH = true;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public retrievePasswordRes retrieve (retrievePasswordParam rp) {
        retrievePasswordRes res = new retrievePasswordRes();
        Connection conn;
        PreparedStatement pst;
        ResultSet ret, ret2;
        int verifyResult = verifyRetrieveParam(rp);
        if (verifyResult == -1) {
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
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            java.util.Calendar cal = java.util.Calendar.getInstance();
            Date date = new Date(cal.getTimeInMillis() - 24*60*60*1000);
            format.setTimeZone(TimeZone.getTimeZone("GMT"));
            String searchTimeStr = format.format(date);
            if (verifyResult == 1) {
                String sql = "SELECT count(pre_id) FROM preRegister WHERE email=? AND platform=? AND expire > ?";
                pst = conn.prepareStatement(sql);
                pst.setString(1, rp.getEmail());
                pst.setString(2, rp.getPlatform());
                pst.setString(3, searchTimeStr);
                ret = pst.executeQuery();
                if (ret.next() && ret.getInt(1) > 3) {
                    res.setAuth(-1);
                    res.setCode(1015);                           // retrieve password too many times
                    return res;
                }
                String searchSql = "SELECT id FROM customerAccount WHERE email=?";
                pst = conn.prepareStatement(searchSql);
                pst.setString(1, rp.getEmail());
                ret2 = pst.executeQuery();
                if (ret2.next()) {
                    int uid = ret2.getInt(1);
                    UUID uuid = UUID.randomUUID();
                    String verifyCode = uuid.toString().substring(24,32);
                    String insertSql = "INSERT INTO preRegister (uid, email, platform, verifyCode, expire) VALUES (?,?,?,?,ADDTIME(utc_timestamp(), '0 00:30:00'));";
                    pst = conn.prepareStatement(insertSql);
                    pst.setInt(1, uid);
                    pst.setString(2, rp.getEmail());
                    pst.setString(3, rp.getPlatform());
                    pst.setString(4, verifyCode);
                    pst.executeUpdate();

                    String context = "<p>验证码: " + verifyCode + "</p><p>系统检测到您正在用此邮箱账号在AGiView竞拍平台找回账号密码，请您不要将此验证码透露给其他人，并在30分钟之内点击如下链接完成密码重置。如非您本人操作，请忽略此邮件。</p>";
                    mailSendUtil mail = new mailSendUtil();
                    if (mail.sendHtmlMail(rp.getEmail(), "AGiView账号密码找回", context)) {
                        res.setAuth(1);
                        res.setCode(0);
                        res.setRetrieve(1);
                        return res;
                    } else {
                        res.setAuth(-1);
                        res.setCode(1013);                       // mail fails to send
                        return res;
                    }
                } else {
                    res.setAuth(-1);
                    res.setCode(1028);                           // not registered
                    return res;
                }
            } else if (verifyResult == 2) {
                String sql = "SELECT count(pre_id) FROM preRegister WHERE tel=? AND tel_country=? AND platform=? AND expire > ?";
                pst = conn.prepareStatement(sql);
                pst.setString(1, rp.getTel());
                pst.setString(2, rp.getTelCountry());
                pst.setString(3, rp.getPlatform());
                pst.setString(4, searchTimeStr);
                ret = pst.executeQuery();
                if (ret.next() && ret.getInt(1) > 3) {
                    res.setAuth(-1);
                    res.setCode(1015);                           // retrieve password too many times
                    return res;
                }
                String searchSql = "SELECT id FROM customerAccount WHERE tel=? AND tel_country=?";
                pst = conn.prepareStatement(searchSql);
                pst.setString(1, rp.getTel());
                pst.setString(2, rp.getTelCountry());
                ret2 = pst.executeQuery();
                if (ret2.next()) {
                    int uid = ret2.getInt(1);
                    StringBuffer verifyCode = new StringBuffer("");
                    for(int i=0; i<6; i++){
                        int tmp = (int)Math.floor(Math.random()*10);
                        verifyCode.append(tmp);
                    }
                    String insertSql = "INSERT INTO preRegister (uid, tel, tel_country, platform, verifyCode, expire) VALUES (?,?,?,?,?,ADDTIME(utc_timestamp(), '0 00:30:00'));";
                    pst = conn.prepareStatement(insertSql);
                    pst.setInt(1, uid);
                    pst.setString(2, rp.getTel());
                    pst.setString(3, rp.getTelCountry());
                    pst.setString(4, rp.getPlatform());
                    pst.setString(5, verifyCode.toString());
                    pst.executeUpdate();
                    // TODO
                    // sending msg module
                    //
                    res.setAuth(1);
                    res.setCode(0);
                    res.setRetrieve(1);
                    if (TEXTSWITCH) {
                        res.setVerifyCode(verifyCode.toString());
                    }
                    return res;
                } else {
                    res.setAuth(-1);
                    res.setCode(1028);                           // not registered
                    return res;
                }
            } else {
                res.setAuth(-1);
                res.setCode(1000);                               // parameters not correct
                return res;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            res.setAuth(-2);
            res.setCode(2000);                                   // mysql error
            return res;
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private int verifyRetrieveParam (retrievePasswordParam rt) {
        try {
            if (rt.getEmail() != null && rt.getPlatform() != null)
                return 1;
            else if (rt.getTel() != null && rt.getTelCountry() != null && rt.getPlatform() != null)
                return 2;
            else return -1;
        } catch (RuntimeException e) {
            return -1;
        }
    }
}
