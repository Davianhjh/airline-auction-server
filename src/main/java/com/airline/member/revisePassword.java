package com.airline.member;

import com.airline.tools.HiKariCPHandler;
import com.airline.tools.UTCTimeUtil;
import org.mindrot.jbcrypt.BCrypt;

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

@Path("/member/revisePassword")
public class revisePassword {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public revisePasswordRes revise (@Context HttpHeaders hh, revisePasswordParam rp) {
        MultivaluedMap<String, String> header = hh.getRequestHeaders();
        String AgiToken = header.getFirst("token");
        revisePasswordRes res = new revisePasswordRes();
        Connection conn;
        PreparedStatement pst, pst2;
        ResultSet ret, ret2;
        boolean verifyResult = verifyRevisePasswordParam(rp);
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
            String verifySql = "SELECT id, password FROM customerToken INNER JOIN customerAccount ON customerToken.uid = customerAccount.id WHERE token = ? and expire > ?;";
            pst = conn.prepareStatement(verifySql);
            pst.setString(1, AgiToken);
            pst.setString(2, utcTimeStr);
            ret = pst.executeQuery();
            if (ret.next()) {
                int uid = ret.getInt(1);
                String password = ret.getString(2);
                if (password == null) {
                    res.setAuth(-1);
                    res.setCode(1023);                          // user's password not set yet
                    return res;
                } else if (!BCrypt.checkpw(rp.getOldPwd(), password)) {
                    res.setAuth(-1);
                    res.setCode(1020);                          // user's old password not match
                    return res;
                } else {
                    String updateSql = "UPDATE customerAccount set password=?, platform=? WHERE id=?";
                    pst = conn.prepareStatement(updateSql);
                    pst.setString(1, BCrypt.hashpw(rp.getNewPwd(), BCrypt.gensalt()));
                    pst.setString(2, rp.getPlatform());
                    pst.setInt(3, uid);
                    pst.executeUpdate();
                    res.setAuth(1);
                    res.setCode(0);
                    res.setRevise(true);
                    return res;
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

    private boolean verifyRevisePasswordParam (revisePasswordParam rp) {
        try {
            return rp.getOldPwd() != null && rp.getNewPwd() != null && rp.getPlatform() != null;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
