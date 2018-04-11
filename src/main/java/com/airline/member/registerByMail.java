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
import java.util.regex.Pattern;

@Path("/member/registerByMail")
public class registerByMail {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public registerByMailRes register (registerByMailParam rm) {
        registerByMailRes res = new registerByMailRes();
        Connection conn;
        PreparedStatement pst;
        ResultSet ret;
        boolean verifyResult = verifyRegisterByMailParams(rm);
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
            String searchSql = "SELECT id FROM customerAccount WHERE email=?";
            pst = conn.prepareStatement(searchSql);
            pst.setString(1, rm.getEmail());
            ret = pst.executeQuery();
            if (ret.next()) {
                conn.close();
                res.setAuth(-1);
                res.setCode(1012);                               // email has been registered
                return res;
            } else {
                UUID uuid = UUID.randomUUID();
                String verifyCode = uuid.toString().substring(24,32);
                String insertSql = "INSERT INTO preRegister (email, password, platform, verifyCode, expire) VALUES (?,?,?,?,ADDTIME(utc_timestamp(), '0 00:30:00'));";
                pst = conn.prepareStatement(insertSql);
                pst.setString(1, rm.getEmail());
                pst.setString(2, BCrypt.hashpw(rm.getPassword(), BCrypt.gensalt()));
                pst.setString(3, rm.getPlatform());
                pst.setString(4, verifyCode);
                pst.executeUpdate();

                String verifyUrl;
                try {
                    Properties serverProp = new Properties();
                    InputStream in = registerByMail.class.getResourceAsStream("/serverAddress.properties");
                    serverProp.load(in);
                    in.close();
                    verifyUrl = serverProp.getProperty("localhostServer") + "/member/verifyMail?verifyCode=" + verifyCode + "&platform=" + rm.getPlatform();
                } catch (IOException e) {
                    res.setAuth(-2);
                    res.setCode(2000);                            // server address properties error
                    return res;
                }
                String context = "<p>系统检测到您正在用此邮箱地址在AGiView竞拍平台注册账号，请您不要将此信息透露给其他人，并在30分钟之内点击如下链接完成邮箱验证。如非您本人操作，请忽略此邮件。</p><a href=\"" + verifyUrl + "\">" + verifyUrl + "</a>";
                mailSendUtil mail = new mailSendUtil();
                if (mail.sendHtmlMail(rm.getEmail(), "AGiView账号邮箱验证", context)) {
                    res.setAuth(1);
                    res.setCode(0);
                    res.setRegister(1);
                    return res;
                } else {
                    res.setAuth(-1);
                    res.setCode(1013);                          // mail fails to send
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

    private boolean verifyRegisterByMailParams (registerByMailParam rm) {
        String emailPattern = "^\\s*\\w+(?:\\.{0,1}[\\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\\.[a-zA-Z]+\\s*$";
        try {
            return rm.getEmail() != null && rm.getPassword() != null && rm.getPlatform() != null && Pattern.matches(emailPattern, rm.getEmail());
        } catch (RuntimeException e) {
            return false;
        }
    }
}
