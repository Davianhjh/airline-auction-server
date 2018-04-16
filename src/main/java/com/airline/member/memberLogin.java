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

@Path("/member/login")
public class memberLogin {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public memberLoginRes login (memberLoginParam ml){
        memberLoginRes res = new memberLoginRes();
        Connection conn;
        PreparedStatement pst;
        ResultSet ret, ret2;

        int verifyResult = verifyMemberLoginParams(ml);
        try {
            conn = HiKariCPHandler.getConn();
        } catch (SQLException e){
            e.printStackTrace();
            res.setAuth(-2);
            res.setCode(2000);                                   // fail to get mysql connection
            return res;
        }
        try {
            if (verifyResult == 0) {
                res.setAuth(-1);
                res.setCode(1000);                               // parameters not correct
                return res;
            } else if (verifyResult == 1) {
                String searchSql = "SELECT id, password, username, cnid_name FROM customerAccount WHERE email=?;";
                pst = conn.prepareStatement(searchSql);
                pst.setString(1, ml.getEmail());
                ret = pst.executeQuery();
            } else {
                String searchSql = "SELECT id, password, username, cnid_name FROM customerAccount WHERE tel_country=? AND tel=?;";
                pst = conn.prepareStatement(searchSql);
                pst.setString(1, ml.getTelCountry());
                pst.setString(2, ml.getTel());
                ret = pst.executeQuery();
            }
            if (ret.next()) {
                int id = ret.getInt(1);
                String hashedPassword = ret.getString(2);
                String userName = ret.getString(3);
                String cnid_name = ret.getString(4);
                if (hashedPassword == null || !BCrypt.checkpw(ml.getPassword(), hashedPassword)) {
                    res.setAuth(-1);
                    res.setCode(1019);                          // user password not match
                    return res;
                }
                String token = tokenHandler.createJWT(String.valueOf(id), userName, ml.getPlatform(), 7 * 24 * 3600 * 1000);
                String sql2 = "SELECT token FROM customerToken WHERE uid=?;";
                pst = conn.prepareStatement(sql2);
                pst.setInt(1, id);
                ret2 = pst.executeQuery();
                if (ret2.next()) {
                    String sql3 = "UPDATE customerToken SET token=?, expire=ADDTIME(utc_timestamp(), '7 00:00:00'), platform=? WHERE uid=?;";
                    pst = conn.prepareStatement(sql3);
                    pst.setString(1, token);
                    pst.setString(2, ml.getPlatform());
                    pst.setInt(3, id);
                    pst.executeUpdate();
                } else {
                    String sql4 = "INSERT INTO customerToken (uid, token, platform, expire) VALUES (?,?,?,ADDTIME(utc_timestamp(), '7 00:00:00'));";
                    pst = conn.prepareStatement(sql4);
                    pst.setInt(1, id);
                    pst.setString(2, token);
                    pst.setString(3, ml.getPlatform());
                    pst.executeUpdate();
                }
                res.setAuth(1);
                res.setCode(0);
                if (cnid_name == null)
                    res.setName(userName);
                else res.setName(cnid_name);
                res.setToken(token);
                return res;
            } else {
                res.setAuth(-1);
                res.setCode(1020);                              // user not found
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

    private int verifyMemberLoginParams(memberLoginParam ml){
        if(verifyEmail(ml) && verifyPassword(ml)){
            return 1;
        }
        else if(verifyTel(ml) && verifyPassword(ml)){
            return 2;
        }
        else return 0;
    }

    private boolean verifyEmail(memberLoginParam ml){
        try {
            return ml.getEmail() != null;
        } catch (RuntimeException e){
            return false;
        }
    }

    private boolean verifyTel(memberLoginParam ml){
        try {
            return ml.getTel() != null && ml.getTelCountry() != null;
        } catch (RuntimeException e){
            return false;
        }
    }

    private boolean verifyPassword(memberLoginParam ml){
        try {
            return ml.getPassword() != null && ml.getPlatform() != null;
        } catch (RuntimeException e){
            return false;
        }
    }
}


