package com.airline.member;

import com.airline.tools.HiKariCPHandler;
import com.airline.tools.UTCTimeUtil;
import com.airline.tools.mailSendUtil;
import org.mindrot.jbcrypt.BCrypt;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Pattern;

@Path("/member/setEmail")
public class setEmail {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public setEmailRes bindEmail (@Context HttpHeaders hh, setEmailParam se) {
        MultivaluedMap<String, String> header = hh.getRequestHeaders();
        String AgiToken = header.getFirst("token");
        setEmailRes res = new setEmailRes();
        Connection conn;
        PreparedStatement pst;
        ResultSet ret;
        boolean verifyResult = verifySetEmailParam(se);
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
            String verifySql = "SELECT id, tel, email FROM customerToken INNER JOIN customerAccount ON customerToken.uid = customerAccount.id WHERE token = ? and expire > ?;";
            pst = conn.prepareStatement(verifySql);
            pst.setString(1, AgiToken);
            pst.setString(2, utcTimeStr);
            ret = pst.executeQuery();
            if (ret.next()) {
                int uid = ret.getInt(1);
                String tel = ret.getString(2);
                String email = ret.getString(3);
                if (tel == null || email != null) {
                    res.setAuth(-1);
                    res.setCode(1023);                       // email has been set
                    return res;
                }
                String searchSql = "SELECT id FROM customerAccount WHERE email=?;";
                pst = conn.prepareStatement(searchSql);
                pst.setString(1, se.getEmail());
                ret = pst.executeQuery();
                if (ret.next()) {
                    res.setAuth(-1);
                    res.setCode(1012);                       // email registered
                    return res;
                } else {
                    UUID uuid = UUID.randomUUID();
                    String verifyCode = uuid.toString().substring(24,32);
                    String insertSql = "INSERT INTO preRegister (uid, email, platform, verifyCode, expire) VALUES (?,?,?,?,ADDTIME(utc_timestamp(), '0 00:30:00'));";
                    pst = conn.prepareStatement(insertSql);
                    pst.setInt(1, uid);
                    pst.setString(2, se.getEmail());
                    pst.setString(3, se.getPlatform());
                    pst.setString(4, verifyCode);
                    pst.executeUpdate();

                    String verifyUrl;
                    try {
                        Properties serverProp = new Properties();
                        InputStream in = setEmail.class.getResourceAsStream("/serverAddress.properties");
                        serverProp.load(in);
                        in.close();
                        verifyUrl = serverProp.getProperty("localhostServer") + "/member/verifyMail?verifyCode=" + verifyCode + "&platform=" + se.getPlatform();
                    } catch (IOException e) {
                        conn.close();
                        res.setAuth(-2);
                        res.setCode(2000);                            // server address properties error
                        return res;
                    }
                    String context = "<p>系统检测到您正在用此邮箱地址绑定AGiView竞拍平台账号" + tel + "，请您不要将此信息透露给其他人，并在30分钟之内点击如下链接完成邮箱验证。如非您本人操作，请忽略此邮件。</p><p>" + verifyUrl + "</p>";
                    mailSendUtil mail = new mailSendUtil();
                    if (mail.sendHtmlMail(se.getEmail(), "AGiView账号邮箱验证", context)) {
                        conn.close();
                        res.setAuth(1);
                        res.setCode(0);
                        res.setBind(1);
                        return res;
                    } else {
                        conn.close();
                        res.setAuth(-1);
                        res.setCode(1013);                            // mail fails to send
                        return res;
                    }
                }
            } else {
                res.setAuth(-1);
                res.setCode(1020);                                    // user not found
                return res;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            res.setAuth(-2);
            res.setCode(2000);                                        // mysql error
            return res;
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean verifySetEmailParam (setEmailParam se) {
        String emailPattern = "^\\s*\\w+(?:\\.{0,1}[\\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\\.[a-zA-Z]+\\s*$";
        try {
            return se.getEmail() != null && se.getPlatform() != null && Pattern.matches(emailPattern, se.getEmail());
        } catch (Exception e) {
            return false;
        }
    }
}
