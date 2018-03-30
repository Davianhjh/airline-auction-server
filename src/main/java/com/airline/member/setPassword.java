package com.airline.member;

import com.airline.tools.HiKariCPHandler;
import com.airline.tools.tokenHandler;
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

@Path("/member/setPassword")
public class setPassword {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public setPasswordRes setPassword (setPasswordParam sp) {
        setPasswordRes res = new setPasswordRes();
        Connection conn;
        PreparedStatement pst;
        ResultSet ret, ret2;

        boolean verifyResult = verifySetPasswordParams(sp);
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
            String searchSql = "SELECT id, username, password FROM customerAccount WHERE tel_country=? AND tel=? AND platform=?;";
            pst = conn.prepareStatement(searchSql);
            pst.setString(1, sp.getTelCountry());
            pst.setString(2, sp.getTel());
            pst.setString(3, sp.getPlatform());
            ret = pst.executeQuery();
            if (ret.next()) {
                int uid = ret.getInt(1);
                String name = ret.getString(2);
                String pwd = ret.getString(3);
                if (pwd != null) {
                    res.setAuth(-1);
                    res.setCode(1021);                        // password has been set
                    return res;
                } else {
                    String sql1 = "UPDATE customerAccount set password=? WHERE id=?;";
                    pst = conn.prepareStatement(sql1);
                    pst.setString(1, BCrypt.hashpw(sp.getPassword(), BCrypt.gensalt()));
                    pst.setInt(2, uid);
                    pst.executeUpdate();

                    String sql2 = "SELECT token from customerToken WHERE uid=?";
                    pst = conn.prepareStatement(sql2);
                    pst.setInt(1, uid);
                    ret2 = pst.executeQuery();
                    String token = tokenHandler.createJWT(String.valueOf(uid), name, sp.getPlatform(), 7 * 24 * 3600 * 1000);
                    if (ret2.next()) {
                        String sql3 = "UPDATE customerToken set token=?, platform=?, expire=ADDTIME(utc_timestamp(), '7 00:00:00') WHERE uid=?";
                        pst = conn.prepareStatement(sql3);
                        pst.setString(1, token);
                        pst.setString(2, sp.getPlatform());
                        pst.setInt(3, uid);
                        pst.executeQuery();
                    } else {
                        String sql4 = "INSERT INTO customerToken (uid, token, platform, expire) VALUES (?,?,?,ADDTIME(utc_timestamp(), '7 00:00:00'));";
                        pst = conn.prepareStatement(sql4);
                        pst.setInt(1, uid);
                        pst.setString(2, token);
                        pst.setString(3, sp.getPlatform());
                        pst.executeUpdate();
                    }
                    res.setAuth(1);
                    res.setCode(0);
                    res.setName(name);
                    res.setToken(token);
                    return res;
                }
            } else {
                res.setAuth(-1);
                res.setCode(1011);                             // account not registered
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

    private boolean verifySetPasswordParams(setPasswordParam sp){
        try {
            return (sp.getTelCountry() != null && sp.getTel() != null && sp.getPlatform() != null && sp.getPassword() != null);
        } catch (RuntimeException e){
            return false;
        }
    }
}
