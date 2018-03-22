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
import java.util.regex.Pattern;

@Path("/member/setEmail")
public class setEmail {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public setEmailRes reviseEmail (@Context HttpHeaders hh, setEmailParam se) {
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
            String verifySql = "SELECT id FROM customerToken INNER JOIN customerAccount ON customerToken.uid = customerAccount.id WHERE token = ? and expire > ?;";
            pst = conn.prepareStatement(verifySql);
            pst.setString(1, AgiToken);
            pst.setString(2, utcTimeStr);
            ret = pst.executeQuery();
            if (ret.next()) {
                int uid = ret.getInt(1);
                String searchSql = "SELECT id FROM customerAccount WHERE email=?;";
                pst = conn.prepareStatement(searchSql);
                pst.setString(1, se.getEmail());
                ret = pst.executeQuery();
                if (ret.next()) {
                    res.setAuth(-1);
                    res.setCode(1010);                          // tel registered
                    return res;
                } else {
                    String updateSql = "UPDATE customerAccount set email=? WHERE id=?;";
                    pst = conn.prepareStatement(updateSql);
                    pst.setString(1, se.getEmail());
                    pst.setInt(2, uid);
                    pst.executeUpdate();
                    res.setAuth(1);
                    res.setCode(0);
                    res.setRevise(1);
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

    private boolean verifySetEmailParam (setEmailParam se) {
        String emailPattern = "^\\s*\\w+(?:\\.{0,1}[\\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\\.[a-zA-Z]+\\s*$";
        try {
            return se.getEmail() != null && Pattern.matches(emailPattern, se.getEmail());
        } catch (Exception e) {
            return false;
        }
    }
}
