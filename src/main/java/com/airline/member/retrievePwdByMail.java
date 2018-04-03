package com.airline.member;

import com.airline.tools.HiKariCPHandler;
import com.airline.tools.mailSendUtil;
import org.mindrot.jbcrypt.BCrypt;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.UUID;

@Path("/member/mail/retrievePassword")
public class retrievePwdByMail {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public retrievePwdByMailRes retrieveByMail (retrievePwdByMailParam rm) {
        retrievePwdByMailRes res = new retrievePwdByMailRes();
        Connection conn;
        PreparedStatement pst;
        ResultSet ret;
        boolean verifyResult = verifyRetrieveParam(rm);
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
            res.setCode(2000);                               // fail to get mysql connection
            return res;
        }
        try {
            String searchSql = "SELECT id FROM customerAccount WHERE email=?";
            pst = conn.prepareStatement(searchSql);
            pst.setString(1, rm.getEmail());
            ret = pst.executeQuery();
            if (ret.next()) {
                int uid = ret.getInt(1);
                UUID uuid = UUID.randomUUID();
                String verifyCode = uuid.toString().substring(24,32);
                String insertSql = "INSERT INTO preRegister (uid, email, platform, verifyCode, expire) VALUES (?,?,?,?,ADDTIME(utc_timestamp(), '0 00:30:00'));";
                pst = conn.prepareStatement(insertSql);
                pst.setInt(1, uid);
                pst.setString(2, rm.getEmail());
                pst.setString(3, rm.getPlatform());
                pst.setString(4, verifyCode);
                pst.executeUpdate();

                String verifyUrl;
                try {
                    Properties serverProp = new Properties();
                    InputStream in = registerByMail.class.getResourceAsStream("/serverAddress.properties");
                    serverProp.load(in);
                    in.close();
                    verifyUrl = serverProp.getProperty("localhostServer") + "/member/mail/retrievePassword?email=" + rm.getEmail() + "&verifyCode=" + verifyCode + "&platform=" + rm.getPlatform();
                } catch (IOException e) {
                    res.setAuth(-2);
                    res.setCode(2000);                            // server address properties error
                    return res;
                }
                String context = "<p>系统检测到您正在用此邮箱账号在AGiView竞拍平台找回账号密码，请您不要将此信息透露给其他人，并在30分钟之内点击如下链接完成密码重置。如非您本人操作，请忽略此邮件。</p><p>" + verifyUrl + "</p>";
                mailSendUtil mail = new mailSendUtil();
                if (mail.sendHtmlMail(rm.getEmail(), "AGiView账号密码找回", context)) {
                    res.setAuth(1);
                    res.setCode(0);
                    res.setRetrieve(1);
                    return res;
                } else {
                    res.setAuth(-1);
                    res.setCode(1013);                          // mail fails to send
                    return res;
                }
            } else {
                res.setAuth(-1);
                res.setCode(1028);                           // not registered
                return res;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            res.setAuth(-2);
            res.setCode(2000);                               // mysql error
            return res;
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean verifyRetrieveParam (retrievePwdByMailParam rm) {
        try {
            return rm.getEmail() != null && rm.getPlatform() != null;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
