package com.airline.member;

import com.airline.tools.HiKariCPHandler;
import com.airline.tools.UTCTimeUtil;

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

@Path("/member/authentication")
public class authentication  {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public authenticationRes authentic (@Context HttpHeaders hh, authenticationParam ap) {
        MultivaluedMap<String, String> header = hh.getRequestHeaders();
        String AgiToken = header.getFirst("token");
        authenticationRes res = new authenticationRes();
        Connection conn;
        PreparedStatement pst;
        ResultSet ret, ret2;
        boolean verifyResult = verifyAuthenticationParams(ap);
        if (AgiToken == null || !verifyResult) {
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
            String verifySql = "SELECT id FROM customerToken INNER JOIN customerAccount ON customerToken.uid = customerAccount.id WHERE token = ? and expire > ?;";
            pst = conn.prepareStatement(verifySql);
            pst.setString(1, AgiToken);
            pst.setString(2, utcTimeStr);
            ret = pst.executeQuery();
            if (ret.next()) {
                int uid = ret.getInt(1);
                String searchSql = "SELECT id FROM customerAccount WHERE cnid=?;";
                pst = conn.prepareStatement(searchSql);
                pst.setString(1, ap.getIdcard());
                ret2 = pst.executeQuery();
                if (ret2.next()) {
                    res.setAuth(-1);
                    res.setCode(1015);                          // idcard has been authenticated
                    return res;
                } else {
                    String sql = "UPDATE customerAccount set cnid=?, cnid_name=?, gender=?, birthday=? WHERE id=?;";
                    pst = conn.prepareStatement(sql);
                    pst.setString(1, ap.getIdcard());
                    pst.setString(2, ap.getName());
                    pst.setString(3, ap.getGender());
                    pst.setString(4, ap.getBirthday());
                    pst.setInt(5, uid);
                    pst.executeUpdate();
                    res.setAuth(1);
                    res.setCode(0);
                    res.setAuthentication(1);
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

    private boolean verifyAuthenticationParams(authenticationParam ap){
        try {
            String name = ap.getName();
            String idcard = ap.getIdcard();
            String birthday = ap.getBirthday();
            return  ((ap.getGender().equals("M") || ap.getGender().equals("F")) && name != null && idcard != null && birthday != null);
        } catch (RuntimeException e) {
            return false;
        }
    }
}
