package com.airline.member;

import com.airline.tools.HiKariCPHandler;
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

@Path("/member/resetPassword")
public class resetPassword {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public resetPasswordRes resetPwd (resetPasswordParam rp) {
        resetPasswordRes res = new resetPasswordRes();
        Connection conn;
        PreparedStatement pst;
        ResultSet ret;

        int verifyResult = verifyResetPasswordParam(rp);
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
                String searchSql = "SELECT id FROM customerAccount where email=? AND password=?;";
                pst = conn.prepareStatement(searchSql);
                pst.setString(1, rp.getEmail());
                pst.setString(2, rp.getVerifyCode());
            } else {
                String searchSql = "SELECT id FROM customerAccount where tel=? AND tel_country=? AND password=?";
                pst = conn.prepareStatement(searchSql);
                pst.setString(1, rp.getTel());
                pst.setString(2, rp.getTelCountry());
                pst.setString(3, rp.getVerifyCode());
            }
            ret = pst.executeQuery();
            if (ret.next()) {
                int uid = ret.getInt(1);
                String updateSql = "UPDATE customerAccount set password=? WHERE id=?";
                pst = conn.prepareStatement(updateSql);
                pst.setString(1, BCrypt.hashpw(rp.getPassword(), BCrypt.gensalt()));
                pst.setInt(2, uid);
                pst.executeUpdate();

                String sql = "UPDATE customerToken set token=null WHERE uid=?";
                pst = conn.prepareStatement(sql);
                pst.setInt(1, uid);
                pst.executeUpdate();

                res.setAuth(1);
                res.setCode(0);
                res.setReset(1);
                return res;
            } else {
                res.setAuth(-1);
                res.setCode(1029);                              // verify not correct
                return res;
            }
        }  catch (SQLException e){
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

    private int verifyResetPasswordParam(resetPasswordParam rp) {
        if (verifyEmail(rp) && verifyPassword(rp)) {
            return 1;
        } else if (verifyTel(rp) && verifyPassword(rp)) {
            return 2;
        } else {
            return 0;
        }
    }

    private boolean verifyEmail(resetPasswordParam rp) {
        try {
            return rp.getEmail() != null;
        } catch (RuntimeException e) {
            return false;
        }
    }

    private boolean verifyTel(resetPasswordParam rp) {
        try {
            return rp.getTel() != null && rp.getTelCountry() != null;
        } catch (RuntimeException e) {
            return false;
        }
    }

    private boolean verifyPassword (resetPasswordParam rp) {
        try {
            return rp.getPassword() != null && rp.getVerifyCode() != null;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
